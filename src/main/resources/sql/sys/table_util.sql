-- Module: System (sys)
-- Section: Table Util
-- Function Description: check if table has any deleted record
-- Params:
--  _table_name
--  _only_me
--  _user_id
create or replace function has_any_deleted_record(_table_name text, _only_me bool, _user_id bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select exists (select from %s where deleted_by is not null and (deleted_by = %L or %L = false))
) as t', _table_name, _user_id, _only_me);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Table Util
-- Function Description: perform restore or forever delete record(s)
-- Params:
--  _table_name
--  _deleted_ids
--  _restore_ids
--  _updated_by
--  -updated_date
create or replace function restore_or_forever_delete(_table_name text, _deleted_ids text, _restore_ids text, _updated_by bigint, _updated_date bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin

if _deleted_ids is not null then
_query = format('
	delete from %s where id in (%s)
', _table_name, _deleted_ids);
execute _query;
end if;

if _restore_ids is not null then
	_query = format('
		update %s set deleted_by = null, deleted_date = null, updated_by=%L, updated_date=%L, access_date=%L where id in (%s)
	', _table_name, _updated_by, _updated_date, _updated_date, _restore_ids);
end if;

execute _query;

return  'ok';
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Table Util
-- Function Description: Get all deleted record(s) of the table
-- Params: 
-- _table_name
-- _columns
-- _only_me: only records of user
-- _user_id
create or replace function get_all_deleted_records(_table_name text, _columns text, _only_me bool, _user_id bigint)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select t.id, %s, h.last_name || '' '' || h.first_name as "deletedBy", t.deleted_date as "deletedDate"
	from %s t
	inner join human_or_org h on h.id = t.deleted_by and (h.id = %L or %L = false)
	where t.deleted_by is not null
	order by %s
) as t', _columns, _table_name, _user_id, _only_me, _columns);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



-- Module: System (sys)
-- Section: Table Util
-- Function Description: Get all record(s) of the table with pagination
-- Params: 
-- _table_name
-- _columns
-- _order_by
-- _page
-- _page_size
-- _only_me: only records of user
-- _user_id
-- _include_disabled
create or replace function get_simple_list(_table_name text, _columns text, _order_by text, _page bigint, _page_size bigint, _only_me bool, _user_id bigint, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
	disabled text;
begin
disabled = get_disabled_cond_str(null, _include_disabled);
_query = format('
	select %s, coalesce(updated_date, created_date) as updated_or_created_date
	from %s
	where deleted_by is null and (%L = false or created_by = %L) and %s
	order by %s
', _columns, _table_name, _only_me, _user_id, disabled, _order_by);
return  paginate(_query, _page, _page_size);
end;
$$ language plpgsql called on null input;



create or replace function select_all_except(_table_name text, _except_columns text)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('select ''select '' || array_to_string(array(select c.column_name::text
        from information_schema.columns As c
            where table_name = %L
            and  c.column_name::text not in(%s)
    ), %L)', _table_name, _except_columns, ',');
execute _query into ret_val;
return ret_val;
end;
$$ language plpgsql called on null input;



create or replace function get_one_by_id(_table_name text, _id bigint)
returns text as $$
declare 
	_query text;
	_qh text;
	ret_val text;
begin
_qh = select_all_except(_table_name, '''password''');
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	%s
	from %s
	where id = %L
) as t', _qh, _table_name, _id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Table Util
-- Function Description: Soft delete one or many record by id
-- Params:
--  _table_name
--  _deleted_ids
--  _user_id
--  _deleted_date
create or replace function soft_delete_many(_table_name text, _deleted_ids text, _user_id bigint, _deleted_date bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
if _deleted_ids is not null then
	_query = format('
		with rows as (
			update %s set deleted_by = %L, deleted_date = %L, access_date = %L where id in (%s)
		returning 1)
		select count(*) from rows;
	', _table_name, _user_id, _deleted_date, _deleted_date, _deleted_ids);
end if;

execute _query into ret_val;
return ret_val;
end;
$$ language plpgsql called on null input;


create or replace function get_all_columns_of_table(_table_name text)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select column_name as "columnName"
	from information_schema.columns
	where table_schema = %L and table_name = %L
) as t', 'public', _table_name);
execute _query into ret_val;
return ret_val;
end;
$$ language plpgsql called on null input;




create or replace function is_text_value_existed(_table_name text, _column_name text, _value text)
returns bool as $$
declare 
	_query TEXT;
	ret_val bool;
begin
_query = format(
'
	select exists (select from %s where lower(%s) = lower(%L) and (%s!='''' and '''' != %L))
', _table_name, _column_name, _value, _column_name, _value);
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function is_text_value_duplicated(_table_name text, _column_name text, _value text, _id bigint)
returns bool as $$
declare 
	_query TEXT;
	ret_val bool;
begin
_query = format(
'
	select exists (select from %s where lower(%s) = lower(%L) and id != %L and (%s!='''' and '''' != %L))
', _table_name, _column_name, _value, _id, _column_name, _value);
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



create or replace function update_table_by_id(_table_name text, _expression text, _id bigint, _updated_by bigint, _updated_date bigint)
returns bigint as $$
declare 
	_query TEXT;
	ret_val bigint;
begin
_query = format('
	with rows as (
		update %s set %s, updated_by = %L, updated_date = %L where id = %L
	returning 1)
	select count(*) from rows;
', _table_name, _expression, _updated_by, _updated_date, _id);

execute _query into ret_val;
return ret_val;
end;
$$ language plpgsql called on null input;



create or replace function get_max_sort(_table_name text)
returns bigint as $$
declare 
	_query TEXT;
	ret_val bigint;
begin
_query = format('
	select coalesce(max(sort),0) from %s
', _table_name);

execute _query into ret_val;
return ret_val;
end;
$$ language plpgsql called on null input;




CREATE OR REPLACE FUNCTION json_query(_query text)
RETURNS TEXT AS $$
DECLARE
	ret_val TEXT;
	full_query TEXT;
BEGIN
full_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	%s
) as t', _query);

EXECUTE full_query INTO ret_val;
RETURN ret_val;
END;
$$ LANGUAGE PLPGSQL CALLED ON NULL INPUT;