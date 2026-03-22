ALTER TABLE news
    ADD COLUMN category VARCHAR(32) NOT NULL DEFAULT 'PRODUCTS';

ALTER TABLE news
    ADD CONSTRAINT chk_news_category CHECK (category IN (
                                                        'PRODUCTS',
                                                        'PRODUCTION',
                                                        'SUPPLIES',
                                                        'PROJECTS',
                                                        'CLIENTS',
                                                        'PARTNERS',
                                                        'PROMOTIONS'
        ));

CREATE INDEX idx_news_category ON news (category);
