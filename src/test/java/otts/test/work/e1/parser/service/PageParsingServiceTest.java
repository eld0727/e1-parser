package otts.test.work.e1.parser.service;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import otts.test.work.e1.parser.dto.JobUnitDTO;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by alex on 08.12.2015.<br/>
 * Page parsing test
 */
public class PageParsingServiceTest {

    private String testPage;

    @Before
    public void setUp() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("test_page.html");
        try {
            testPage = IOUtils.toString(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    public void testParsingSinglePage() throws Exception {
        PageParsingService parsingService = new PageParsingService();
        PageParsingService.ParseResult parseResult = parsingService.parsePage(testPage);
        assertNotNull("parse result could not be null", parseResult);

        List<String> pages = parseResult.getPages();
        assertNotNull("parse result pages could not be null", pages);
        assertEquals("should be 1 page to move forward", 1, pages.size());

        String expectedPage = "http://ekb.zarplata.ru/vacancy?state%5B%5D=1&state%5B%5D=4&average_salary=1&categories_facets=1&city_id%5B%5D=994&highlight=1&q=%D0%A3%D0%B1%D0%BE%D1%80%D1%89%D0%B8%D0%BA&searched_q=%D0%A3%D0%B1%D0%BE%D1%80%D1%89%D0%B8%D0%BA&limit=10&offset=10";
        assertEquals("should be correct href", expectedPage, pages.get(0));

        List<JobUnitDTO> jobs = parseResult.getJobs();
        assertNotNull("parse result jobs could not be null", jobs);
        assertEquals("should be 10 jobs", 10, jobs.size());


        assertJobs("jobs[0]:", jobs.get(0), JobUnitDTO.builder()
                .title("Уборщик")
                .companyName("МАОУ СОШ № 200")
                .minSalary(7100L)
                .maxSalary(15000L)
                .address("Екатеринбург, Ботанический район, Крестинского, 39, метро «Ботаническая»")
                .build()
        );

        assertJobs("jobs[1]:", jobs.get(1), JobUnitDTO.builder()
                .title("Уборщик помещений")
                .companyName("Уральский техникум \"Рифей\"")
                .minSalary(7100L)
                .maxSalary(7100L)
                .address("Екатеринбург, Уктус район, Корейский пер., 6, метро «Ботаническая»")
                .build()
        );

        assertJobs("jobs[2]:", jobs.get(2), JobUnitDTO.builder()
                .title("Уборщик снега")
                .companyName("«Торговая Группа Альянс»")
                .minSalary(17000L)
                .maxSalary(null)
                .address("Екатеринбург, Пионерский район, Шоферов, 7")
                .build()
        );

        assertJobs("jobs[3]:", jobs.get(3), JobUnitDTO.builder()
                .title("Уборщик помещений в автоцентр \"Тойота Восток\"")
                .companyName("Тойота Центр Екатеринбург Восток")
                .minSalary(16200L)
                .maxSalary(16200L)
                .address("Екатеринбург, Сибирский тракт, 24б")
                .build()
        );

        assertJobs("jobs[4]:", jobs.get(4), JobUnitDTO.builder()
                .title("Уборщик")
                .companyName("ООО \"СервисТрансКлининг-Урал\"")
                .minSalary(7000L)
                .maxSalary(8000L)
                .address("Екатеринбург, Заречный район, Черепанова , метро «Уральская»")
                .build()
        );

        assertJobs("jobs[5]:", jobs.get(5), JobUnitDTO.builder()
                .title("Уборщик производственных помещений")
                .companyName("ЗАО \"Комбинат пищевой \"Хороший вкус\"")
                .minSalary(15000L)
                .maxSalary(null)
                .address("Екатеринбург")
                .build()
        );

        assertJobs("jobs[6]:", jobs.get(6), JobUnitDTO.builder()
                .title("Уборщик территории 2 разряда")
                .companyName("Государственное автономное учреждение Свердловской области \"Уральская футбольная академия\"")
                .minSalary(16000L)
                .maxSalary(null)
                .address("Екатеринбург, Уралмаш район, Фестивальная, 10, Вход со стороны ДК \"Уралмаш\", метро «Уралмаш»")
                .build()
        );

        assertJobs("jobs[7]:", jobs.get(7), JobUnitDTO.builder()
                .title("Уборщик территории")
                .companyName("ООО \"Мечел-Сервис\" (Екатеринбургский филиал)")
                .minSalary(10000L)
                .maxSalary(null)
                .address("Екатеринбург, Пионерский район, Учителей, 37, метро «Машиностроителей»")
                .build()
        );

        assertJobs("jobs[8]:", jobs.get(8), JobUnitDTO.builder()
                .title("Уборщица, уборщик")
                .companyName("Ирландский паб в Карасьеозёрский-2")
                .minSalary(15000L)
                .maxSalary(null)
                .address("Екатеринбург, ВИЗ район, Малогородская, 4, метро «Геологическая»")
                .build()
        );

        assertJobs("jobs[9]:", jobs.get(9), JobUnitDTO.builder()
                .title("Уборщик территории")
                .companyName("УИ(ф)РАНХиГС")
                .minSalary(6000L)
                .maxSalary(null)
                .address("Екатеринбург, 8Марта, метро «Геологическая»")
                .build()
        );

    }


    private void assertJobs(String prefix, JobUnitDTO actual, JobUnitDTO expected) {
        assertEquals(prefix + " titles should be equals", expected.getTitle(), actual.getTitle());
        assertEquals(prefix + " min salaries should be equals", expected.getMinSalary(), actual.getMinSalary());
        assertEquals(prefix + " max salaries should be equals", expected.getMaxSalary(), actual.getMaxSalary());
        assertEquals(prefix + " company names should be equals", expected.getCompanyName(), actual.getCompanyName());
        assertEquals(prefix + " addresses should be equals", expected.getAddress(), actual.getAddress());
    }
}