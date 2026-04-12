CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           customer_number VARCHAR(50) NOT NULL UNIQUE,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           phone_number VARCHAR(20) NOT NULL UNIQUE,
                           email VARCHAR(150) UNIQUE,
                           national_id VARCHAR(30) NOT NULL UNIQUE,
                           status VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL
);


CREATE TABLE customer_limits (
                                 id BIGSERIAL PRIMARY KEY,
                                 customer_id BIGINT NOT NULL UNIQUE,
                                 max_limit NUMERIC(19,2) NOT NULL,
                                 available_limit NUMERIC(19,2) NOT NULL,
                                 utilized_limit NUMERIC(19,2) NOT NULL,
                                 effective_from DATE NOT NULL,
                                 effective_to DATE,
                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP NOT NULL,
                                 CONSTRAINT fk_customer_limit_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);