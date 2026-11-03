
CREATE OR REPLACE VIEW v_recipient_i18n AS
SELECT *
-- http://www.sql-tutorial.ru/ru/book_crosstab.html
FROM crosstab(
         'select r.id,
   i18n.lang                 AS lang,
   i18n.text                 AS txt
from recipient r
     left join rel_recipient__title rt on r.id = rt.recipients_id
     left join i_18_n i18n on rt.titles_id = i18n.id
ORDER BY 1, CASE lang WHEN ''RU'' THEN 1 WHEN ''UZ'' THEN 2 ELSE 3 END'
         ) AS ct (id bigint, ru varchar(64), uz varchar(64), en varchar(64));
