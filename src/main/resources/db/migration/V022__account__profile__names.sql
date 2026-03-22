ALTER TABLE account
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN patronymic VARCHAR(255);

UPDATE account
SET first_name = COALESCE(NULLIF(trim(name), ''), ''),
    last_name  = ''
WHERE first_name IS NULL;

UPDATE account
SET last_name = ''
WHERE last_name IS NULL;

ALTER TABLE account
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;

UPDATE account
SET name = NULLIF(trim(concat_ws(' ',
                                 NULLIF(trim(last_name), ''),
                                 NULLIF(trim(first_name), ''),
                                 NULLIF(trim(patronymic), ''))), '');
