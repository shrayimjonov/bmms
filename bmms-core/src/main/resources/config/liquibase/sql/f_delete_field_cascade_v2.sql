CREATE OR REPLACE FUNCTION f_delete_field_cascade(p_field_id bigint)
    RETURNS integer AS
$$
declare
    res    INTEGER;
    fv_row record;
BEGIN

    RAISE NOTICE 'p_field_id : %', p_field_id;

    delete from rel_field__title where field_id in (select f.id from field f where f.recipient_id = p_field_id) or field_id = p_field_id ;

    delete from rel_field__description where field_id in (select f.id from field f where f.recipient_id = p_field_id) or  field_id = p_field_id;

    FOR fv_row IN
        SELECT *
        FROM field_value fv
        WHERE fv.field_id = p_field_id
        LOOP
            res = f_delete_field_value_cascade(fv_row.id);
        END LOOP;

    --     RETURN res;

    delete from prefix where field_id = p_field_id;

    delete from field f where f.id = p_field_id;

    RETURN 1;
END;
$$
    LANGUAGE plpgsql;

-- select * from delete_recipient_cascade('Beeline');

-- select * from recipient where short_name = 'Beeline';
