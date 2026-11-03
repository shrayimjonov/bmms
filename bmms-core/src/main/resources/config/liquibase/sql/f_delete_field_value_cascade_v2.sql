CREATE OR REPLACE FUNCTION f_delete_field_value_cascade(p_fv_id bigint) returns integer
    language plpgsql
as
$$
declare
    fvt_row record;
BEGIN

    RAISE NOTICE 'p_fv_id : %', p_fv_id;

    FOR fvt_row IN
        select * from field_value_title where field_values_id = p_fv_id
        LOOP
            RAISE NOTICE 'Deleting field_value_title where field_values_id = %', fvt_row.field_values_id;
            delete from field_value_title where field_values_id = fvt_row.field_values_id;
            delete from i_18_n where id in (fvt_row.titles_id);
        END LOOP;

    FOR fvt_row IN
        select * from field_value_description where field_values_id = p_fv_id
        LOOP
            RAISE NOTICE 'Deleting field_value_description where field_values_id = %', fvt_row.field_values_id;
            delete from field_value_description where field_values_id = fvt_row.field_values_id;
            delete from i_18_n where id in (fvt_row.descriptions_id);
        END LOOP;

    RAISE NOTICE 'Deleting field_value where field_id = %', p_fv_id;
    delete from field_value where id = p_fv_id;

    RETURN 1;
END;
$$ LANGUAGE plpgsql;
