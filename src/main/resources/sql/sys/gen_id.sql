create sequence global_id_sequence;

DROP FUNCTION IF EXISTS id_generator(OUT result bigint);
CREATE OR REPLACE FUNCTION id_generator(OUT result bigint) AS $$
DECLARE
    our_epoch bigint := 1314220021721;
    seq_id bigint;
    now_millis bigint;
    -- the id of this DB shard, must be set for each
    -- schema shard you have - you could pass this as a parameter too
    shard_id int := 1;
BEGIN
    SELECT nextval('public.global_id_sequence') % 1024 INTO seq_id;

    SELECT FLOOR(EXTRACT(EPOCH FROM clock_timestamp()) * 1000) INTO now_millis;
    result := (now_millis - our_epoch) << 23;
    result := result | (shard_id << 10);
    result := result | (seq_id);
END;
$$ LANGUAGE PLPGSQL;




CREATE OR REPLACE FUNCTION gen_code(
	prefix character varying,
	table_name character varying,
	company bigint, len smallint)
RETURNS TEXT
LANGUAGE 'plpgsql'
COST 100
VOLATILE 
AS $BODY$
DECLARE cur_id BIGINT;
DECLARE ret TEXT;
DECLARE last_year_text TEXT;
DECLARE p_name TEXT;

BEGIN
	p_name = table_name||company||'0'||'_'||date_part('year', now());
	last_year_text = right(date_part('year', now())::text, 4);
	SELECT T."value" INTO cur_id 
	FROM seq T 
	WHERE T ."name" = p_name LIMIT 1 ;
	IF cur_id IS NULL THEN 
		cur_id := 1 ;
		INSERT INTO seq ("table_name", "name", "value") VALUES(table_name, p_name, cur_id) ;
	ELSE
		cur_id := cur_id + 1 ;
		UPDATE seq SET "value" = cur_id WHERE "name" = p_name ;
	END IF ;
	ret := prefix ||'-'||last_year_text||'-'|| to_char(cur_id, 'fm' || REPEAT('0', len));
	RETURN ret;
END;
$BODY$;

ALTER TABLE tsk_task ADD COLUMN code TEXT DEFAULT gen_code('TASK', 'tsk_task', 1, 5::smallint);




