package otts.test.work.e1.parser.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by alex on 08.12.2015.<br/>
 * DTO for job row
 */
@Data
@Builder
@EqualsAndHashCode(exclude = "description")
@ToString(of = {"title", "companyName", "minSalary", "maxSalary"})
public class JobUnitDTO {

    /**
     * Title of the job
     */
    private String title;

    /**
     * Minimal salary in rubles
     */
    private Long minSalary;

    /**
     * Maximal salary in rubles
     */
    private Long maxSalary;

    /**
     * Employer company name
     */
    private String companyName;

    /**
     * Employer company address
     */
    private String address;

    /**
     * Job description
     */
    private String description;

}
