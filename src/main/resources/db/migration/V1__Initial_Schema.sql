-- V1__Initial_Schema.sql

-- ============================================================
-- PETS
-- ============================================================
CREATE TABLE pets (
    id                 BIGSERIAL PRIMARY KEY,
    external_id        VARCHAR(100) UNIQUE,
    type               VARCHAR(50)  NOT NULL,
    gender             VARCHAR(50)  NOT NULL DEFAULT 'unknown',
    size               VARCHAR(50)  NOT NULL,
    age                VARCHAR(50)  NOT NULL,
    good_with_children BOOLEAN      NOT NULL DEFAULT false,
    name               VARCHAR(200),
    description        TEXT,
    status             VARCHAR(50)  NOT NULL DEFAULT 'available',
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PET PHOTOS
-- ============================================================
CREATE TABLE pet_photos (
    id         BIGSERIAL PRIMARY KEY,
    pet_id     BIGINT  NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    file_path  VARCHAR(500) NOT NULL,
    url        VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- CUSTOMERS
-- ============================================================
CREATE TABLE customers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    phone      VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ADOPTION REQUESTS
-- ============================================================
CREATE TABLE adoption_requests (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT NOT NULL REFERENCES customers(id),
    pet_id       BIGINT NOT NULL REFERENCES pets(id),
    status       VARCHAR(50) NOT NULL DEFAULT 'pending',
    notes        TEXT,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_adoption_customer_pet UNIQUE (customer_id, pet_id)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_pets_type          ON pets(type);
CREATE INDEX idx_pets_gender        ON pets(gender);
CREATE INDEX idx_pets_size          ON pets(size);
CREATE INDEX idx_pets_age           ON pets(age);
CREATE INDEX idx_pets_good_with_ch  ON pets(good_with_children);
CREATE INDEX idx_pets_status        ON pets(status);
CREATE INDEX idx_pet_photos_pet_id  ON pet_photos(pet_id);
CREATE INDEX idx_customers_phone    ON customers(phone);
CREATE INDEX idx_adoption_requested ON adoption_requests(requested_at);
CREATE INDEX idx_adoption_customer  ON adoption_requests(customer_id);
CREATE INDEX idx_adoption_pet       ON adoption_requests(pet_id);