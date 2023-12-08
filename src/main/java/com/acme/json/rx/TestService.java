package com.acme.json.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.helidon.webserver.Routing;
import io.helidon.webserver.Service;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.spi.JsonProvider;

@SuppressWarnings("SpellCheckingInspection")
public class TestService implements Service {

    private static final String[] FIRST_NAMES = new String[] {
            "Athena", "Breanne", "Cindy", "Claribel", "Dash",
            "Everly", "Gallagher", "Giles", "Giselle", "Jewell", "Joy",
            "Kingsley", "Lily", "Lizette", "Luna", "Merrion", "Mikey",
            "Rickie", "Stella", "Terra"
    };

    private static final String[] LAST_NAMES = new String[] {
            "Amelia", "Basil", "Bonita", "Claudia", "Fredrick",
            "Georgina", "Jordin", "Krysten", "Lanny", "Lilah",
            "Macy", "Maeghan", "Maitland", "Megan", "Oakley",
            "Remington", "Timmy", "Tylar", "Viviette", "Yoland"
    };

    private static final String[] ROLES = new String[] {
            "Global Infrastructure Coordinator", "Future Intranet Supervisor",
            "Product Optimization Developer", "Customer Mobility Assistant",
            "Direct Identity Planner", "Legacy Communications Developer",
            "National Implementation Director", "Customer Applications Engineer",
            "Senior Tactics Agent", "Legacy Optimization Administrator",
            "National Assurance Technician", "Investor Directives Representative",
            "Senior Group Executive", "Legacy Markets Developer",
            "Regional Markets Architect", "Future Resonance Technician",
            "District Tactics Specialist", "Senior Group Developer",
            "Senior Configuration Specialist", "Future Markets Planner"
    };

    final JsonProvider jsonProvider = JsonProvider.provider();
    final Jsonb jsonb = JsonbBuilder.newBuilder()
            .withProvider(JsonProviderRx.create(jsonProvider))
            .build();

    final Random random = new Random();

    @Override
    public void update(Routing.Rules rules) {
        rules.get((req, res) -> {
            int size = req.queryParams().first("size").map(Integer::parseInt).orElse(2);
            List<Employee> employees = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                employees.add(new Employee(
                        FIRST_NAMES[random.nextInt(FIRST_NAMES.length)],
                        LAST_NAMES[random.nextInt(LAST_NAMES.length)],
                        ROLES[random.nextInt(ROLES.length)]
                ));
            }
            Company entity = new Company("Acme Corp.", employees);
            JsonMultiOutputStream stream = JsonMultiOutputStream.create();
            jsonb.toJson(entity, stream);
            res.send(stream);
        });
    }

    public record Company(String name, List<Employee> employees) {
    }

    public record Employee(String firstName, String lastName, String role) {
    }
}
