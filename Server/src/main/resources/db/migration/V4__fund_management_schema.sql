-- Migration script for Fund Management feature

-- Create budgets table
CREATE TABLE budgets (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    fiscal_year INT NOT NULL,
    fiscal_period VARCHAR(50),
    total_amount DECIMAL(12,2) NOT NULL,
    allocated_amount DECIMAL(12,2) DEFAULT 0.00,
    remaining_amount DECIMAL(12,2),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2,
    CONSTRAINT fk_budgets_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create fund_allocations table
CREATE TABLE fund_allocations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    budget_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    previous_amount DECIMAL(12,2) DEFAULT 0.00,
    allocation_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    allocated_by BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    notes VARCHAR(500),
    CONSTRAINT fk_fund_allocations_budget FOREIGN KEY (budget_id) REFERENCES budgets(id),
    CONSTRAINT fk_fund_allocations_program FOREIGN KEY (program_id) REFERENCES scholarship_programs(id),
    CONSTRAINT fk_fund_allocations_allocated_by FOREIGN KEY (allocated_by) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_budgets_fiscal_year ON budgets(fiscal_year);
CREATE INDEX idx_budgets_status ON budgets(status);
CREATE INDEX idx_budgets_created_by ON budgets(created_by);
CREATE INDEX idx_fund_allocations_budget_id ON fund_allocations(budget_id);
CREATE INDEX idx_fund_allocations_program_id ON fund_allocations(program_id);
CREATE INDEX idx_fund_allocations_allocated_by ON fund_allocations(allocated_by);

-- Update scholarship_programs table to add fund tracking columns
ALTER TABLE scholarship_programs 
ADD allocated_amount DECIMAL(12,2) DEFAULT 0.00,
    used_amount DECIMAL(12,2) DEFAULT 0.00,
    remaining_amount DECIMAL(12,2) DEFAULT 0.00;

-- Update existing records to use the default values
UPDATE scholarship_programs 
SET allocated_amount = 0.00,
    used_amount = 0.00,
    remaining_amount = 0.00
WHERE allocated_amount IS NULL OR used_amount IS NULL OR remaining_amount IS NULL;

-- Sample data for testing
-- Insert a draft budget
INSERT INTO budgets (fiscal_year, fiscal_period, total_amount, remaining_amount, start_date, end_date, description, status, created_by, created_at)
VALUES (2025, 'Q1-Q2', 100000.00, 100000.00, '2025-01-01', '2025-06-30', 'Budget for first half of 2025', 'DRAFT', 1, GETDATE());

-- Insert an active budget
INSERT INTO budgets (fiscal_year, fiscal_period, total_amount, remaining_amount, start_date, end_date, description, status, created_by, created_at)
VALUES (2024, 'Full Year', 200000.00, 200000.00, '2024-01-01', '2024-12-31', 'Annual budget for 2024', 'ACTIVE', 1, GETDATE());

-- Insert a closed budget
INSERT INTO budgets (fiscal_year, fiscal_period, total_amount, allocated_amount, remaining_amount, start_date, end_date, description, status, created_by, created_at)
VALUES (2023, 'Full Year', 150000.00, 150000.00, 0.00, '2023-01-01', '2023-12-31', 'Annual budget for 2023', 'CLOSED', 1, GETDATE());

-- Insert fund allocations
-- Allocate funds from the active budget to the existing scholarship program
INSERT INTO fund_allocations (budget_id, program_id, amount, allocation_date, allocated_by, status, notes)
VALUES (2, 1, 50000.00, GETDATE(), 1, 'APPROVED', 'Initial allocation for Merit Scholarship');

-- Update the allocated and remaining amounts in the budget
UPDATE budgets SET allocated_amount = 50000.00, remaining_amount = 150000.00 WHERE id = 2;

-- Update the allocated and remaining amounts in the scholarship program
UPDATE scholarship_programs SET allocated_amount = 50000.00, remaining_amount = 50000.00 WHERE id = 1;
