CREATE OR REPLACE FUNCTION f_delete_gateway_id_cascade(p_gw_id bigint)
    RETURNS INTEGER AS
$$
declare
    res           INTEGER;
    --     gId     bigint;
    recipient_row record;
BEGIN

    RAISE NOTICE 'gId : %', p_gw_id;

    FOR recipient_row IN
        SELECT *
        FROM recipient a
        WHERE a.gateway_id = p_gw_id
        LOOP
            res = f_delete_recipient_id_cascade(recipient_row.id);
        END LOOP;

    RETURN res;
END;
$$
    LANGUAGE plpgsql;

-- select * from delete_recipient_cascade('Beeline');

-- select * from recipient where short_name = 'Beeline';
