package otts.test.work.e1.parser.service;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.SneakyThrows;
import org.apache.commons.collections.MapUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Service;
import otts.test.work.e1.parser.dto.JobUnitDTO;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 08.12.2015.<br/>
 * Service for parsing incoming e1 pages
 */
@Service
public class PageParsingService {

    /**
     * It is thread safe, so we can use one instance of {@link HtmlCleaner}
     */
    private HtmlCleaner htmlCleaner = new HtmlCleaner();

    /**
     * Parse single page for job descriptions and pages to move forward
     */
    @SneakyThrows(XPatherException.class)
    public ParseResult parsePage(String page) {
        TagNode root = htmlCleaner.clean(page);
        ParseResult.ParseResultBuilder builder = ParseResult.builder();

        extractPages(root, builder);
        extractJobs(root, builder);
        return builder.build();
    }

    /**
     * Extracts jobs on the page
     *
     * @param root    root node of the page
     * @param builder {@link ParseResult.ParseResultBuilder}
     * @throws XPatherException possible exception of {@link TagNode#evaluateXPath}
     */
    private void extractJobs(TagNode root, ParseResult.ParseResultBuilder builder) throws XPatherException {
        Object[] scripts = root.evaluateXPath("//script[@nonce]/text()");
        for (Object script : scripts) {
            String trimmedScript = script.toString().trim();
            if (trimmedScript.startsWith("var SEARCH_DATA")) {
                extractDataFromScript(trimmedScript, builder);
            }
        }
    }

    /**
     * Execute script that contains data about jobs and extract them
     *
     * @param script  script with data
     * @param builder {@link ParseResult.ParseResultBuilder}
     */
    @SneakyThrows(ScriptException.class)
    private void extractDataFromScript(String script, ParseResult.ParseResultBuilder builder) {
        ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
        nashorn.eval(script); //TODO possible optimization is to evaluate only required part
        //noinspection unchecked
        List<ScriptObjectMirror> jobs = (List<ScriptObjectMirror>) nashorn.eval("Java.to(LIST.vacancies||[],'java.util.List');");
        for (ScriptObjectMirror job : jobs) {
            builder.job(convertJob(job));
        }
    }

    /**
     * Converts job object to {@link JobUnitDTO}
     *
     * @param job job {@link ScriptObjectMirror} from parsed script
     */
    private JobUnitDTO convertJob(ScriptObjectMirror job) {
        Long minSalary = MapUtils.getLong(job, "salary_min_rub");
        Long maxSalary = MapUtils.getLong(job, "salary_max_rub");
        String paymentType = MapUtils.getString(job, "payment_type_alias");
        if ("fixed".equals(paymentType)) {
            maxSalary = minSalary;
        }

        Map contact = (Map) job.get("contact");
        String address = MapUtils.getString(contact, "address");
        Map subway = (Map) contact.get("subway");
        if(subway != null) {
            String title = MapUtils.getString(subway, "title");
            if(title != null) {
                address = address + ", метро \u00ab" + title + "\u00bb";
            }
        }

        return JobUnitDTO.builder()
                .title(makePrettyString(MapUtils.getString(job, "header")))
                .minSalary(minSalary == 0L ? null : minSalary)
                .maxSalary(maxSalary == 0L ? null : maxSalary)
                .companyName(makePrettyString(MapUtils.getString((Map) job.get("company"), "title")))
                .address(makePrettyString(address))
                .description(MapUtils.getString(job, "description"))
                .build();
    }

    /**
     * Remove redundant white spaces and unescape special characters
     */
    private String makePrettyString(String s) {
        return unescapeString(s).trim().replaceAll("\\s+", " ");
    }

    /**
     * In some strings they escape special symbols for some reasons, so we unescape it
     */
    private String unescapeString(String s) {
        return s.replaceAll("&quot;", "\"")
                .replaceAll("&ls;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&apos;", "'")
                .replaceAll("&amp;", "&");
    }

    /**
     * Extracts only page links that comes after active page
     *
     * @param root    root node of the page
     * @param builder {@link ParseResult.ParseResultBuilder}
     * @throws XPatherException possible exception of {@link TagNode#evaluateXPath}
     */
    private void extractPages(TagNode root, ParseResult.ParseResultBuilder builder) throws XPatherException {
        Object[] pageElements = root.evaluateXPath("//ul[@class='ra-pagination-pages']/li/*[@rv-text='page:page']");
        boolean meetActivePage = false;
        for (Object pageElement : pageElements) {
            if (pageElement instanceof TagNode) {
                TagNode node = (TagNode) pageElement;
                String name = node.getName();
                if ("span".equals(name)) {
                    meetActivePage = true;
                } else if (meetActivePage && "a".equals(name)) {
                    builder.page(node.getAttributeByName("href"));
                }
            }
        }
    }


    /**
     * Result of single page parsing
     */
    @Data
    @Builder
    public static class ParseResult {

        /**
         * founded pages to move forward
         */
        @Singular
        private List<String> pages;

        /**
         * founded jobs
         */
        @Singular
        private List<JobUnitDTO> jobs;
    }
}
