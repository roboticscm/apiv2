-- Module: System (sys)
-- Section: Menu (mnu)
-- Function Description: Get assigned role menu list by user id and department id
-- Params:
--  _user_id
--  _dep_id: Department ID
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_roled_menu_list_by_user_id_and_dep_id(_user_id bigint, _dep_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
if is_system_admin_by_user_id(_user_id, false, false) = true then
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
select distinct m.id as "menuId",
	m.name as "menuName",
	m.path as "path",
	m.sort,
	m.created_date,
	m.font_icon as "fontIcon",
	m.icon_data as "iconData",
	m.use_font_icon as "useFontIcon",
	mh.last_access as "lastAccess",
	mh.dep_id as "departmentId",
	ono.name as "departmentName"
from menu m
left join menu_history mh on mh.menu_id = m.id and (mh.dep_id = %L or %L is null) and mh.human_id = %L
inner join menu_org mo on mo.menu_id = m.id and (mo.org_id = %L or %L is null) and ' || get_deleted_cond_str('mo', _include_deleted) || '
inner join owner_org ono on ono.id = mo.org_id and ono.deleted_by is null and ono.disabled = false and (ono.id = mh.dep_id or %L is not null)
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
where ' || get_deleted_cond_str('m', _include_deleted) || '
	and ' || get_disabled_cond_str('m', _include_disabled) || '
order by mh.last_access desc NULLS LAST, m.sort, m.name, m.created_date	
) as t', _dep_id, _dep_id, _user_id, _dep_id, _dep_id, _dep_id);
else
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
select distinct m.id as "menuId",
	m.name as "menuName",
	m.path as "path",
	m.sort,
	m.created_date,
	m.font_icon as "fontIcon",
	m.icon_data as "iconData",
	m.use_font_icon as "useFontIcon",
	mh.last_access as "lastAccess",
	mh.dep_id as "departmentId",
	ono.name as "departmentName"
from menu m
inner join menu_org mo on mo.menu_id = m.id and (mo.org_id = %L or %L is null) and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '
left join menu_history mh on mh.menu_id = m.id and (mh.dep_id = %L or %L is null) and mh.human_id = %L
inner join owner_org ono on ono.id = mo.org_id and ono.deleted_by is null and ono.disabled = false and (ono.id = mh.dep_id or %L is not null)
where ' || get_deleted_cond_str('m', _include_deleted) || '
	and ' || get_disabled_cond_str('m', _include_disabled) || '
order by mh.last_access desc NULLS LAST, m.sort, m.name, m.created_date	
) as t', _dep_id, _dep_id, _user_id, _dep_id, _dep_id, _user_id, _dep_id);
end if;
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



-- Module: System (sys)
-- Section: Menu (mnu)
-- Function Description: Get assigned role menu path list by user id 
-- Params:
--  _user_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_roled_menu_path_list_by_user_id(_user_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
if is_system_admin_by_user_id(_user_id, false, false) = true then
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
select distinct path as "path"
from menu
	where ' || get_deleted_cond_str(null, _include_deleted) || '
	and ' || get_disabled_cond_str(null, _include_disabled) || '
) as t';
else
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
select distinct m.path as "path"
from menu m
inner join menu_org mo on mo.menu_id = m.id  and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '
where ' || get_deleted_cond_str('m', _include_deleted) || '
	and ' || get_disabled_cond_str('m', _include_disabled) || '
) as t', _user_id);
end if;
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;

-- Module: System (sys)
-- Section: Menu (mnu)
-- Function Description: Get the first assigned role menu path by user id and department id
-- Params:
--  _user_id
--  _dep_id: Department ID
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_first_roled_menu_path_by_user_id_and_dep_id(_user_id bigint,  _dep_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
if is_system_admin_by_user_id(_user_id, false, false) = true then
_query = format('
select array_to_string(array(select m.path 
from menu m
inner join menu_org mo on mo.menu_id = m.id and mo.org_id = %L and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
where m.path != %L and m.path is not null
order by m.sort, m.name, m.created_date	
limit 1
), %L)', _dep_id, '', ',');
else
_query = format('
select array_to_string(array(select m.path 
from menu m
inner join menu_org mo on mo.menu_id = m.id and mo.org_id = %L and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '
where ' || get_deleted_cond_str('m', _include_deleted) || '
	and ' || get_disabled_cond_str('m', _include_disabled) || '
	and m.path != %L and m.path is not null
order by m.sort, m.name, m.created_date
	limit 1
), %L)', _dep_id, _user_id, '', ',');
end if;
execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;




create or replace function sys_is_menu_path_assigned_for_user(_menu_path text, _user_id bigint, _include_deleted bool, _include_disabled bool)
returns bool as $$
declare 
	_query TEXT;
	ret_val bool;
begin
if is_system_admin_by_user_id(_user_id, false, false) = true then
	ret_val = true;
else
_query = format('
select exists(select m.id 
from menu m
inner join menu_org mo on mo.menu_id = m.id and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '
where m.path = %L and ' || get_deleted_cond_str('m', _include_deleted) || '
	and ' || get_disabled_cond_str('m', _include_disabled) || '
order by m.sort, m.name, m.created_date
	limit 1
)', _user_id, _menu_path);

execute _query into ret_val;
end if;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Menu (mnu)
-- Function Description: Get all menu list including deleted, disabled record
-- Params: 
--  _sort_by_created_date: true sort by create date, false: sort by sort field and name
create or replace function sys_get_all_menu_list(_sort_by_created_date bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
	sort TEXT;
begin
if _sort_by_created_date = true then 
	sort = 'created_date desc';
else
	sort = 'sort, name';
end if;
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
	select id, code, name, path, deleted_by as "deletedBy", disabled
	from menu
	where deleted_by is null
	order by ' || sort || '
) as t';

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


