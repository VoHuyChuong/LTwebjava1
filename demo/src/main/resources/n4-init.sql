-- Script khoi tao Nhom IV, chay moi lan app khoi dong (qua dung ket noi cua Spring Boot).
-- Moi cau lenh ket thuc bang dau cham phay va tu bo qua neu da co, nen chay nhieu lan khong sao.
-- Quy tac: khong dung GO, khong dung USE, khong dat dau cham phay trong chu thich.

-- 1. Bang tam departments / majors (chi tao khi chua co bang that cua ban khac)
IF OBJECT_ID('dbo.departments', 'U') IS NULL CREATE TABLE dbo.departments (id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, name NVARCHAR(255) NULL, deleted_at DATETIME2 NULL);

IF OBJECT_ID('dbo.majors', 'U') IS NULL CREATE TABLE dbo.majors (id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, name NVARCHAR(255) NULL, deleted_at DATETIME2 NULL);

-- Chi them du lieu mau khi bang dang rong
IF NOT EXISTS (SELECT 1 FROM dbo.departments) INSERT INTO dbo.departments (id, name) VALUES ('11111111-1111-1111-1111-111111111111', N'Khoa CNTT');

IF NOT EXISTS (SELECT 1 FROM dbo.majors) INSERT INTO dbo.majors (id, name) VALUES ('22222222-0000-0000-0000-000000000001', N'Công nghệ thông tin'), ('22222222-0000-0000-0000-000000000002', N'Kỹ thuật phần mềm'), ('22222222-0000-0000-0000-000000000003', N'Hệ thống thông tin'), ('22222222-0000-0000-0000-000000000004', N'Quản trị kinh doanh'), ('22222222-0000-0000-0000-000000000005', N'Tài chính - Ngân hàng'), ('22222222-0000-0000-0000-000000000006', N'Ngôn ngữ Anh');

-- 2. Bang training_programs: neu la ban cu (admission_year kieu DATE) thi xoa de tao lai dung kieu INT
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.training_programs') AND name = 'admission_year' AND TYPE_NAME(system_type_id) = 'date') DROP TABLE dbo.training_programs;

IF OBJECT_ID('dbo.training_programs', 'U') IS NULL CREATE TABLE dbo.training_programs (
    id UNIQUEIDENTIFIER NOT NULL CONSTRAINT pk_training_programs PRIMARY KEY CONSTRAINT df_tp_id DEFAULT NEWID(),
    code NVARCHAR(100) NOT NULL,
    name NVARCHAR(255) NOT NULL,
    name_en NVARCHAR(255) NULL,
    major_id UNIQUEIDENTIFIER NOT NULL,
    department_id UNIQUEIDENTIFIER NOT NULL,
    degree_level NVARCHAR(50) NULL,
    education_type NVARCHAR(50) NULL,
    total_credits DECIMAL(5,1) NULL,
    required_credits DECIMAL(5,1) NULL,
    elective_credits DECIMAL(5,1) NULL,
    internship_credits DECIMAL(5,1) NULL,
    thesis_credits DECIMAL(5,1) NULL,
    admission_year INT NULL,
    duration_years DECIMAL(5,1) NULL,
    max_duration_years DECIMAL(5,1) NULL,
    effective_date DATE NULL,
    expiry_date DATE NULL,
    description NVARCHAR(MAX) NULL,
    objectives NVARCHAR(MAX) NULL,
    learning_outcomes NVARCHAR(MAX) NULL,
    version NVARCHAR(20) NULL,
    status NVARCHAR(20) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT df_tp_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NULL,
    created_by UNIQUEIDENTIFIER NULL,
    updated_by UNIQUEIDENTIFIER NULL,
    deleted_at DATETIME2 NULL,
    deleted_by UNIQUEIDENTIFIER NULL,
    is_active BIT NOT NULL CONSTRAINT df_tp_is_active DEFAULT 1,
    CONSTRAINT ck_tp_duration CHECK (max_duration_years IS NULL OR duration_years IS NULL OR max_duration_years >= duration_years),
    CONSTRAINT ck_tp_dates CHECK (effective_date IS NULL OR expiry_date IS NULL OR expiry_date > effective_date),
    CONSTRAINT ck_tp_credits CHECK ((total_credits IS NULL OR total_credits >= 0) AND (required_credits IS NULL OR required_credits >= 0) AND (elective_credits IS NULL OR elective_credits >= 0) AND (internship_credits IS NULL OR internship_credits >= 0) AND (thesis_credits IS NULL OR thesis_credits >= 0))
);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_training_programs_code' AND object_id = OBJECT_ID('dbo.training_programs')) CREATE UNIQUE INDEX ux_training_programs_code ON dbo.training_programs (code) WHERE deleted_at IS NULL;

-- 3. Du lieu mau 6 chuong trinh (chi them khi bang dang rong)
IF NOT EXISTS (SELECT 1 FROM dbo.training_programs) INSERT INTO dbo.training_programs (code, name, name_en, major_id, department_id, degree_level, education_type, total_credits, required_credits, elective_credits, internship_credits, thesis_credits, admission_year, duration_years, max_duration_years, effective_date, expiry_date, description, objectives, learning_outcomes, version, status) VALUES
('IT_2023', N'Công nghệ thông tin', N'Information Technology', '22222222-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', N'Đại học', N'Chính quy', 150.0, 120.0, 20.0, 5.0, 5.0, 2023, 4.5, 6.5, '2023-09-05', '2028-09-05', N'Chương trình Công nghệ thông tin', N'Đào tạo kỹ sư phần mềm', N'Lập trình full-stack', N'v1.0', N'Đang đào tạo'),
('SE_2023', N'Kỹ thuật phần mềm', N'Software Engineering', '22222222-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', N'Đại học', N'Chất lượng cao', 152.0, 122.0, 20.0, 5.0, 5.0, 2023, 4.0, 6.0, '2023-09-05', '2027-09-05', N'Chương trình Kỹ thuật phần mềm', N'Đào tạo kỹ sư phần mềm', N'Lập trình thành thạo', N'v1.0', N'Đang đào tạo'),
('IS_2023', N'Hệ thống thông tin', N'Information Systems', '22222222-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', N'Đại học', N'Chính quy', 148.0, 118.0, 20.0, 5.0, 5.0, 2023, 4.0, 6.0, '2023-09-05', '2027-09-05', N'Chương trình Hệ thống thông tin', N'Đào tạo phân tích dữ liệu', N'Thiết kế CSDL', N'v1.0', N'Đang đào tạo'),
('BA_2023', N'Quản trị kinh doanh', N'Business Administration', '22222222-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', N'Đại học', N'Chính quy', 135.0, 105.0, 20.0, 5.0, 5.0, 2023, 4.0, 6.0, '2023-09-05', '2027-09-05', N'Chương trình Quản trị kinh doanh', N'Cung cấp kiến thức quản trị', N'Có tư duy chiến lược', N'v1.0', N'Đang đào tạo'),
('FB_2023', N'Tài chính - Ngân hàng', N'Finance and Banking', '22222222-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', N'Đại học', N'Chính quy', 138.0, 108.0, 20.0, 5.0, 5.0, 2023, 4.0, 6.0, '2023-09-05', '2027-09-05', N'Chương trình Tài chính - Ngân hàng', N'Đào tạo quản trị tài chính', N'Phân tích tài chính', N'v1.0', N'Đang đào tạo'),
('EL_2023', N'Ngôn ngữ Anh', N'English Language', '22222222-0000-0000-0000-000000000006', '11111111-1111-1111-1111-111111111111', N'Đại học', N'Chính quy', 130.0, 100.0, 20.0, 5.0, 5.0, 2023, 4.0, 6.0, '2023-09-05', '2027-09-05', N'Chương trình Ngôn ngữ Anh', N'Đào tạo tiếng Anh thương mại', N'Biên phiên dịch chuyên nghiệp', N'v1.0', N'Đang đào tạo');