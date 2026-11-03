CREATE OR REPLACE VIEW v_recipient_i18n AS
    SELECT 1 AS id, 'dummy-recipient' AS ru, 'dummy-recipient' AS uz, 'dummy-recipient' AS en
    UNION ALL
    SELECT 2 AS id, 'dummy-recipient-usd' AS ru, 'dummy-recipient-usd' AS uz, 'dummy-recipient-usd' AS en;
