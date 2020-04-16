-- Module: System (sys)
-- Section: Locale Resource (res)
-- Function Description: Get Locale Resource list by company id and locale
-- Params:
--  _company_id
--  _locale
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_locale_resource_list_by_company_id_and_locale(_company_id bigint, _locale text, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
select id, key, value, category, type_group as "typeGroup"
from locale_resource
where company_id = %L
	and locale = %L
	and ' || get_deleted_cond_str(null, _include_deleted) || '
	and ' || get_disabled_cond_str(null, _include_disabled) || '
) as t', _company_id, _locale);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;




create or replace function sys_get_used_languages()
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
	select distinct ls.locale as "id",
		lang.name 
	from locale_resource ls
	inner join language lang on lang.locale = ls.locale
) as t';

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function sys_get_used_lang_categories(_text_search text)
returns text as $$
declare 
	_query text;
	ret_val text;
	cond text;
begin
	cond = '';

	if _text_search is not null then 
		cond = ' and lower(category) like unaccent(lower(''%' || _text_search || '%''))';
	end if;
	raise notice '%', cond;
	_query = format('select coalesce(json_agg(t), ''[]'')::text 
	from(
		select distinct category as id, category as name 
		from locale_resource 
		where deleted_by is null and disabled = false %s
		order by category
	) as t', cond);

	execute _query into ret_val;
	return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function sys_get_used_lang_type_groups(_text_search text)
returns text as $$
declare 
	_query text;
	ret_val text;
	cond text;
begin
	cond = '';

	if _text_search is not null then 
		cond = ' and lower(type_group) like unaccent(lower(''%' || _text_search || '%''))';
	end if;
	raise notice '%', cond;
	_query = format('select coalesce(json_agg(t), ''[]'')::text 
	from(
		select distinct type_group as id, type_group as name 
		from locale_resource 
		where deleted_by is null and disabled = false %s
		order by type_group
	) as t', cond);

	execute _query into ret_val;
	return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function sys_get_all_languages(_include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
	select * from language
	where ' || get_deleted_cond_str(null, _include_deleted) || '
	and ' || get_disabled_cond_str(null, _include_disabled) || '
) as t';

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Locale Resource (res)
-- Function Description: Get Locale Resource list by company id, category, type group and search text on key or value
-- Params:
--  _company_id
--  _category
--  _type_group
--  _text_search
create or replace function sys_get_locale_resource_by_company_id_and_cat_and_type_group(_company_id bigint, _category text, _type_group text, _text_search text)
returns text as $$
declare 
	_query text;
	ret_val text;
	cond text;
begin
	cond = '';
	if _category is not null then
		cond = ' and category = ''' ||  _category || '''';
	end if;
	
 	if _type_group is not null then
 		cond = cond || ' and type_group = ''' || _type_group || '''';
 	end if;
	
 	if _text_search is not null then
 		cond = cond || ' and (unaccent(lower(key)) like unaccent(lower(''%' ||  _text_search ||  '%'')) or unaccent(lower(value)) like unaccent(lower(''%' ||  _text_search ||  '%'')))';
 	end if;
--raise notice 'Value: %', cond;
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select locale, category, type_group as "typeGroup", key, value
	from locale_resource
	where company_id = %L and  disabled = false and deleted_by is null %s
	order by locale, category, type_group, key
) as t', _company_id,  cond);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Locale Resource (res)
-- Function Description: Get Locale Resource list by company id, category, type group and search text on key or value with pagination
-- Params:
--  _company_id
--  _category
--  _type_group
--  _text_search
--  _page
--  _page_size
create or replace function sys_get_locale_resource_by_company_id_and_cat_and_type_group(_company_id bigint, _category text, _type_group text, _text_search text, _page bigint, _page_size bigint)
returns text as $$
declare 
	_query text;
	ret_val text;
	cond text;
begin
	cond = '';
	if _category is not null then
		cond = ' and category = ''' ||  _category || '''';
	end if;
	
 	if _type_group is not null then
 		cond = cond || ' and type_group = ''' || _type_group || '''';
 	end if;
	
 	if _text_search is not null then
 		cond = cond || ' and (unaccent(lower(type_group)) like unaccent(lower(''%' ||  _text_search ||  '%'')) or unaccent(lower(category)) like unaccent(lower(''%' ||  _text_search ||  '%'')) or unaccent(lower(key)) like unaccent(lower(''%' ||  _text_search ||  '%'')) or unaccent(lower(value)) like unaccent(lower(''%' ||  _text_search ||  '%'')))';
 	end if;
	
_query = format(
	'select locale, category, type_group as "typeGroup", key, value
	from locale_resource
	where company_id = %L and  disabled = false and deleted_by is null
	%s
	order by locale, category, type_group, key
	', _company_id, cond);

return  paginate(_query, _page, _page_size);
end;
$$ language plpgsql called on null input;