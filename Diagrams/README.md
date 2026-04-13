# Diagram Sources

This folder contains the source `.puml` files for the milestone diagrams:

- `C4_Level_1_System_Context.puml`
- `C4_Level_2_Containers.puml`
- `DDD_Domain_Model.puml`

Rendered PNG exports are also included:

- `C4_Level_1_System_Context.png`
- `C4_Level_2_Containers.png`
- `DDD_Domain_Model.png`

Notes:

- The legacy exported images already present in [`diagram`](/C:/Users/Vikit/IdeaProjects/Healthcare_Milestone_ms/diagram) were kept untouched.
- The C4 Level 2 source reflects the final milestone database split used by the runnable codebase: MySQL for `patient-service`, PostgreSQL for `doctor-service`, and PostgreSQL for `clinic-room-service`.
- The C4 files use `!includeurl` to load C4-PlantUML, so rerendering them requires PlantUML with internet access or locally vendored C4 templates.
