create
    or replace function f_delete_field_value_cascade(p_fv_id bigint) returns integer
    language plpgsql
as
$$
declare

BEGIN

    RAISE
        NOTICE 'p_fv_id : %', p_fv_id;

    delete
    from rel_field_value__title
    where field_value_id = p_fv_id;
--           (select id from field_value fv where fv.id = p_fv_id);

    delete
    from rel_field_value__description
    where field_value_id = p_fv_id;
--           (select id from field_value fv where fv.id = p_fv_id);

    delete
    from field_value
    where id = p_fv_id;

    RETURN 1;
END;
$$;

alter function f_delete_field_value_cascade(bigint) owner to bm_ms;

