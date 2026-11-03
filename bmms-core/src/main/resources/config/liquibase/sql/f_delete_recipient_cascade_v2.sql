CREATE OR REPLACE FUNCTION f_delete_recipient_cascade(recipient_name text)
    RETURNS integer AS
$$
declare
    rId       bigint;
    res       INTEGER;
    field_row record;
BEGIN

    select id into rId from recipient r where r.short_name = recipient_name;

    RAISE NOTICE 'rId : %', rId;

    delete from bookkeeping_template bt where bt.recipient_id = recipient_id;

    delete from gateway_param where recipient_id = rId;

    delete from rel_recipient__title where recipient_id = rId;

    delete from rel_recipient__full_name where recipient_id = rId;

    FOR field_row IN
        SELECT *
        FROM field f
        WHERE f.recipient_id = rId
        LOOP
            res = f_delete_field_cascade(field_row.id);
        END LOOP;

    delete from agent_recipient a where a.recipient_id = rId;

    delete from recipient r where r.id = rId;

    RETURN 1;
END;
$$
    LANGUAGE plpgsql;

-- select * from delete_recipient_cascade('Beeline');

-- select * from recipient where short_name = 'Beeline';
