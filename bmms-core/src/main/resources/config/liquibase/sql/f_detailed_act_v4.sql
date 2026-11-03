create or replace function f_detailed_act_v4(p_start_date text, p_end_date text, p_pay_types text, p_gateways text,
                                           p_agents text, p_request_groups text, p_request_sub_accounts text,
                                           p_statuses text)
    returns TABLE
            (
                id             bigint,
                date           timestamp with time zone,
                s_merchant_id  text,
                s_terminal_id  text,
                s_gateway      character varying,
                s_account      character varying,
                r_merchant_id  text,
                r_terminal_id  text,
                r_gateway      character varying,
                recipient      character varying,
                r_account      character varying,
                login          character varying,
                status         character varying,
                request_amount bigint,
                gateway_amount double precision,
                commission     bigint,
                fee            bigint,
                pay_type       character varying
            )
    language plpgsql
as
$$
declare

BEGIN


    return query
        select t.id,
               t.date::timestamp AT TIME ZONE '+5',
               left(t.epos_merchant, strpos(t.epos_merchant, '>') - 1),
               left(t.epos_terminal, strpos(t.epos_terminal, '>') - 1),
               t.sender_gateway,
               t.sender_account,
               right(t.epos_merchant, length(t.epos_merchant) - strpos(t.epos_merchant, '>')) as recipient_merchant,
               right(t.epos_terminal, length(t.epos_terminal) - strpos(t.epos_terminal, '>')) as recipient_terminal,
               t.recipient_gateway,
               t.recipient_name,
               t.recipient_account,
               t.request_login,
               t.status,
               t.request_amount / 100,
               t.gateway_amount / t.amount_ratio,
               t.system_commission_up_amount / 100,
               t.system_fee_amount / 100,
               t.pay_type
        from transaction as t
        where (p_statuses IS NULL OR p_statuses = '' OR
               t.status = any (string_to_array(p_statuses, ',') :: varchar(255)[]))
          AND (p_pay_types IS NULL OR p_pay_types = '' OR
               t.pay_type = any (string_to_array(p_pay_types, ',') :: varchar(255)[]))
          AND (p_gateways IS NULL OR p_gateways = '' OR
               t.recipient_gateway = any (string_to_array(p_gateways, ',') :: varchar(255)[]))
          AND (p_agents IS NULL OR p_agents = '' OR
               t.request_login = any (string_to_array(p_agents, ',') :: varchar(255)[]))
          AND (p_request_groups IS NULL OR p_request_groups = '' OR
               t.request_group = any (string_to_array(p_request_groups, ',') :: varchar(255)[]))
          AND (p_request_sub_accounts IS NULL OR p_request_sub_accounts = '' OR
               t.request_sub_account = any (string_to_array(p_request_sub_accounts, ',') :: varchar(255)[]))
          AND t.jhi_date between timezone('+5', $1::timestamptz) AND timezone('+5', $2::timestamptz)
        order by t.id;
END;
$$;

alter function f_detailed_act3(text, text, text, text, text, text, text, text) owner to postgres;
