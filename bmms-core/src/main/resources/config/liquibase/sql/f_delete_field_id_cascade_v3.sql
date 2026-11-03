create
    or replace function f_delete_field_id_cascade(p_field_id bigint) returns integer
    language plpgsql
as
$$
declare
    res INTEGER;
    field_value_row
        record;
BEGIN

    RAISE
        NOTICE 'Field Id to delete : %', p_field_id;

    delete
    from rel_field__description rfd
    where rfd.field_id = p_field_id;
    delete
    from rel_field__title rft
    where rft.field_id = p_field_id;

    --     SELECT * into field_value_row
--     FROM field_value v
--     WHERE v.field_id = p_field_id;

--     FOR field_value_row IN
--         SELECT *
--         FROM field_value v
--         WHERE v.field_id = p_field_id
--         LOOP
--             RAISE NOTICE 'Field Value to delete XXX : %',field_value_row.id;
--             res = f_delete_field_value_cascade(field_value_row.id);
--         END LOOP;

    FOR field_value_row IN
        SELECT *
        FROM field_value v
        WHERE v.field_id = p_field_id
        LOOP
            res = f_delete_field_value_cascade(field_value_row.id);
        END LOOP;

    delete
    from field r
    where r.id = p_field_id;

    RETURN 1;
END ;
$$;

alter function f_delete_field_id_cascade(bigint) owner to bm_ms;

