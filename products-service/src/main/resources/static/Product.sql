CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          code VARCHAR(50) NOT NULL UNIQUE,
                          name VARCHAR(100) NOT NULL,
                          description VARCHAR(500),
                          tenure_type VARCHAR(20) NOT NULL,
                          min_tenure INT NOT NULL,
                          max_tenure INT NOT NULL,
                          active BOOLEAN NOT NULL,
                          fixed_tenure_allowed BOOLEAN NOT NULL,
                          flexible_tenure_allowed BOOLEAN NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL
);


CREATE TABLE product_fees (
                              id BIGSERIAL PRIMARY KEY,
                              product_id BIGINT NOT NULL,
                              fee_name VARCHAR(100) NOT NULL,
                              fee_type VARCHAR(20) NOT NULL,
                              calculation_type VARCHAR(20) NOT NULL,
                              amount NUMERIC(19,2),
                              percentage NUMERIC(5,2),
                              application_stage VARCHAR(30) NOT NULL,
                              days_after_due INT,
                              active BOOLEAN NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL,
                              CONSTRAINT fk_product_fee_product FOREIGN KEY (product_id) REFERENCES products(id)
);