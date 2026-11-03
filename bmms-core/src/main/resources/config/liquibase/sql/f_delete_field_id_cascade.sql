CREATE OR REPLACE FUNCTION f_delete_field_id_cascade(p_field_id bigint)
    RETURNS integer AS
$$
declare

BEGIN

    RAISE NOTICE 'Field Id to delete : %', p_field_id;

    delete from field_description where fields_id = p_field_id;
    delete from field_title where fields_id = p_field_id;

    delete from field r where r.id = p_field_id;

    RETURN 1;
END;
$$
    LANGUAGE plpgsql;

-- select * from f_delete_field_cascade(223802);
