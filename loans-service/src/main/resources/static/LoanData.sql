CREATE TABLE loans (
                       id BIGSERIAL PRIMARY KEY,
                       loan_number VARCHAR(50) NOT NULL UNIQUE,
                       customer_id BIGINT NOT NULL,
                       product_id BIGINT NOT NULL,
                       principal_amount NUMERIC(19,2) NOT NULL,
                       disbursed_amount NUMERIC(19,2) NOT NULL,
                       total_fees NUMERIC(19,2) NOT NULL,
                       total_paid NUMERIC(19,2) NOT NULL,
                       balance NUMERIC(19,2) NOT NULL,
                       tenure INT NOT NULL,
                       disbursement_date DATE NOT NULL,
                       due_date DATE NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);


CREATE TABLE loan_repayments (
                                 id BIGSERIAL PRIMARY KEY,
                                 loan_id BIGINT NOT NULL,
                                 amount_paid NUMERIC(19,2) NOT NULL,
                                 payment_reference VARCHAR(100) NOT NULL,
                                 payment_channel VARCHAR(50) NOT NULL,
                                 status VARCHAR(20) NOT NULL,
                                 payment_date TIMESTAMP NOT NULL,
                                 CONSTRAINT fk_loan_repayment_loan FOREIGN KEY (loan_id) REFERENCES loans(id)
);

CREATE TABLE loan_fees (
                           id BIGSERIAL PRIMARY KEY,
                           loan_id BIGINT NOT NULL,
                           fee_type VARCHAR(20) NOT NULL,
                           fee_name VARCHAR(100) NOT NULL,
                           amount NUMERIC(19,2) NOT NULL,
                           applied_at TIMESTAMP NOT NULL,
                           reason VARCHAR(255),
                           CONSTRAINT fk_loan_fee_loan FOREIGN KEY (loan_id) REFERENCES loans(id)
);