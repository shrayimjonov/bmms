CREATE OR REPLACE FUNCTION f_delete_recipient_id_cascade(p_recipient_id bigint)
    RETURNS integer AS
$$
declare
    --     rId       bigint;
    res       INTEGER;
    field_row record;
BEGIN

    RAISE NOTICE 'rId : %', p_recipient_id;

    delete from gateway_param gp where gp.recipient_id = p_recipient_id;

    delete from recipient_title rt where rt.recipients_id = p_recipient_id;

    delete from recipient_full_name rfn where rfn.recipients_id = p_recipient_id;

    FOR field_row IN
        SELECT *
        FROM field f
        WHERE f.recipient_id = p_recipient_id
        LOOP
            res = f_delete_field_cascade(field_row.id);
        END LOOP;

    delete from agent_recipient a where a.recipient_id = p_recipient_id;

    delete from recipient r where r.id = p_recipient_id;

    RETURN 1;
END;
$$
    LANGUAGE plpgsql;

-- select * from delete_recipient_cascade('Beeline');

-- select * from recipient where short_name = 'Beeline';

-- DROP FUNCTION f_delete_recipient_id_cascade(bigint)
