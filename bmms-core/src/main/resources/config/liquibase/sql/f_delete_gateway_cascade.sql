CREATE OR REPLACE FUNCTION f_delete_gateway_cascade(gateway_name text)
    RETURNS INTEGER AS
$$
declare
    res     INTEGER;
    gId     bigint;
    temprow record;
BEGIN

    select id into gId from gateway g where g.name = gateway_name;

    RAISE NOTICE 'gId : %', gId;

    FOR temprow IN
        SELECT *
        FROM recipient a
        WHERE a.gateway_id = gId
        LOOP
            res = f_delete_recipient_cascade(temprow.short_name);
        END LOOP;

    RETURN res;
END;
$$
    LANGUAGE plpgsql;

-- select * from delete_recipient_cascade('Beeline');

-- select * from recipient where short_name = 'Beeline';
