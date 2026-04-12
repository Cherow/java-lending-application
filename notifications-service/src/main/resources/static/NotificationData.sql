CREATE TABLE notification_templates (
                                        id BIGSERIAL PRIMARY KEY,
                                        code VARCHAR(100) NOT NULL UNIQUE,
                                        name VARCHAR(100) NOT NULL,
                                        event_type VARCHAR(30) NOT NULL,
                                        channel VARCHAR(20) NOT NULL,
                                        subject VARCHAR(200),
                                        body_template VARCHAR(2000) NOT NULL,
                                        active BOOLEAN NOT NULL,
                                        created_at TIMESTAMP NOT NULL,
                                        updated_at TIMESTAMP NOT NULL
);



CREATE TABLE notification_logs (
                                   id BIGSERIAL PRIMARY KEY,
                                   customer_id BIGINT,
                                   loan_id BIGINT,
                                   template_id BIGINT,
                                   event_type VARCHAR(30) NOT NULL,
                                   channel VARCHAR(20) NOT NULL,
                                   recipient VARCHAR(200) NOT NULL,
                                   subject VARCHAR(200),
                                   message VARCHAR(2000) NOT NULL,
                                   status VARCHAR(20) NOT NULL,
                                   error_message VARCHAR(500),
                                   sent_at TIMESTAMP,
                                   created_at TIMESTAMP NOT NULL,
                                   CONSTRAINT fk_notification_log_template FOREIGN KEY (template_id) REFERENCES notification_templates(id)
);