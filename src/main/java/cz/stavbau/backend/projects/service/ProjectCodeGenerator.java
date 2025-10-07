package cz.stavbau.backend.projects.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectCodeGenerator {

    private final JdbcTemplate jdbc;

    public String nextCode(UUID companyId, LocalDate refDate) {
        int year = (refDate != null ? refDate : LocalDate.now()).getYear();

        Integer seq = jdbc.queryForObject(
                """
                insert into project_code_counters(company_id, year, value)
                values (?, ?, 1)
                on conflict (company_id, year)
                do update set value = project_code_counters.value + 1
                returning value
                """,
                Integer.class, companyId, year
        );

        int n = (seq != null) ? seq : 1;
        return format(year, n);
    }

    protected String format(int year, int n) {
        // MVP šablona: R{YYYY}-{NNN}  (snadno vyměníme za firemní pattern v budoucnu)
        return "R" + year + "-" + String.format("%03d", n);
    }
}
