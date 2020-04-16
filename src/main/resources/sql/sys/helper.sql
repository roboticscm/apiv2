DROP FUNCTION IF EXISTS get_deleted_cond_str(OUT result TEXT, _table TEXT, _include_deleted bool);
CREATE OR REPLACE FUNCTION get_deleted_cond_str(OUT result TEXT, _table TEXT, _include_deleted bool) AS $$
DECLARE
	cond_table TEXT = '';
BEGIN
    if _table is not null then
		cond_table = _table || '.';
	end if;
	result = '((' || cond_table || 'deleted_by is null and ' || _include_deleted || '= false) or ' || _include_deleted || ' = true)';
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;


DROP FUNCTION IF EXISTS get_disabled_cond_str(OUT result TEXT, _table TEXT, _include_disabled bool);
CREATE OR REPLACE FUNCTION get_disabled_cond_str(OUT result TEXT, _table TEXT, _include_disabled bool) AS $$
DECLARE
	cond_table TEXT = '';
BEGIN
    if _table is not null then
		cond_table = _table || '.';
	end if;
	result = '((' || cond_table || 'disabled = false and ' || _include_disabled || ' = false) or ' || _include_disabled  || '= true)';
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;


create or replace function is_system_admin_by_account(_account text, _include_deleted bool, _include_disabled bool)
returns bool as $$
declare 
	_query TEXT;
	ret_val bool;
begin
_query = format(
'
select exists(
	select id from global_param where key=%L and value=%L
	and ' || get_deleted_cond_str(null, _include_deleted) || '
	and ' || get_disabled_cond_str(null, _include_disabled) || '
	limit 1)
', 'SYS_ADMIN_ACCOUNT', _account);
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



create or replace function is_system_admin_by_user_id(_user_id bigint, _include_deleted bool, _include_disabled bool)
returns bool as $$
declare 
	_query TEXT;
	ret_val bool;
begin
_query = format(
'
select exists(
	select id from global_param where key=%L
	and value=(
		select username from human_or_org where id = %L
		and ' || get_deleted_cond_str(null, _include_deleted) || '
		and ' || get_disabled_cond_str(null, _include_disabled) || '
	)
	and ' || get_deleted_cond_str(null, _include_deleted) || '
	and ' || get_disabled_cond_str(null, _include_disabled) || '
	limit 1)
', 'SYS_ADMIN_ACCOUNT', _user_id);
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



create or replace function paginate(_query text, _page bigint, _page_size bigint)
returns text as $$
declare 
	__query text;
	ret_val text;
	paging text;
	json_build text;
begin
	json_build = 'select json_build_object(''payload'', coalesce(jsonb_agg(payload), ''[]''), ''fullCount'', coalesce(max(fullCount), 0))::text';
	paging = '';
	if _page_size != -1 and _page > 0 then
		paging = 'limit ' || _page_size || ' offset ' || _page_size*(_page - 1);
	end if;
__query = format('%s 
from( 
	select to_json(tt) as payload, count(*) over() as fullCount
	from (%s) as tt
	%s
) as t', json_build, _query, paging);

execute __query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;