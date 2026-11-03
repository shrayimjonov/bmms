CREATE OR REPLACE FUNCTION f_delete_field_value_cascade(p_fv_id bigint)
    RETURNS integer AS
$$
declare
    --   p_fv_id bigint;
BEGIN

    RAISE NOTICE 'p_fv_id : %', p_fv_id;

    delete
    from rel_field_value__title
    where field_value_id in
          (select id from field_value fv where fv.field_id = p_fv_id);

    delete
    from rel_field_value__description
    where field_value_id in
          (select id from field_value fv where fv.field_id = p_fv_id);

    delete from field_value where field_id = p_fv_id;

    RETURN 1;
END;
$$
    LANGUAGE plpgsql;

-- select * from delete_recipient_cascade('Beeline');

-- select * from recipient where short_name = 'Beeline';
