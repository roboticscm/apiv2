-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get all sub org ids
-- Params:
--  _parent_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_sub_org_ids(_parent_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare
	ret_val text;
	_query text;
begin
_query = format('with recursive org as (
    select id
    from owner_org
    where parent_id = %L or ( parent_id is null and %L is null)
    	and '|| get_deleted_cond_str(null, _include_deleted) || ' 
	  	and ' || get_disabled_cond_str(null, _include_deleted) || ' 
    union
    select oo.id
    from owner_org oo 
    inner join org o on o.id = oo.parent_id
    where ' || get_deleted_cond_str('oo', _include_deleted) || '
	  	and ' || get_disabled_cond_str(null, _include_deleted) || '
)

select array_to_string(array(select id
from org), %L)', _parent_id, _parent_id, ',');

execute _query into ret_val;
if _parent_id is not null then
	if ret_val = '' then
		ret_val = _parent_id;
	else 
		ret_val = _parent_id || ',' || ret_val;
	end if;
end if;

return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get all parent org ids
-- Params:
--  _id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_all_parent_org_ids(_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare
	ret_val text;
	_query text;
begin
_query = format('with recursive org as (
    select parent_id
    from owner_org
    where id = %L 
    	and '|| get_deleted_cond_str(null, _include_deleted) || ' 
	  	and ' || get_disabled_cond_str(null, _include_deleted) || ' 
    union
    select oo.parent_id
    from owner_org oo 
    inner join org o on o.parent_id = oo.id
    where ' || get_deleted_cond_str('oo', _include_deleted) || '
	  	and ' || get_disabled_cond_str(null, _include_deleted) || '
)

select array_to_string(array(select parent_id
from org), %L)', _id, ',');

execute _query into ret_val;

if ret_val = '' then
	ret_val = _id;
else 
	ret_val = _id || ',' || ret_val;
end if;

return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get owner org tree
-- Params:
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_owner_org_tree(_include_deleted bool, _include_disabled bool)
returns text as $$
with recursive org as (
   	select id, parent_id as "pId", name, type, font_icon as "fontIcon", use_font_icon as "useFontIcon", icon_data as "iconData", true as "open", sort
   	from owner_org
   	where parent_id is null
		and ((deleted_by is null and _include_deleted = false) or _include_deleted = true)
		and ((disabled = false and _include_disabled = false) or _include_disabled = true)
   	union
   	select oo.id, oo.parent_id, oo.name, oo.type, oo.font_icon as "fontIcon", oo.use_font_icon as "useFontIcon", oo.icon_data as "iconData", true as "open", oo.sort
   	from owner_org oo
   	inner join org o ON o.id = oo.parent_id
   	where ((oo.deleted_by is null and _include_deleted = false) or _include_deleted = true)
		and ((oo.disabled = false and _include_disabled = false) or _include_disabled = true)
) 
select coalesce(json_agg(t), '[]')::text
from(
	select *
	from org
	order by sort
) as t;
$$
language sql;



-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get Department tree by menu id
-- Params:
--  _menu_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_department_tree_by_menu_id(_menu_id bigint)
returns text as $$
with recursive org as (
   	select c.id, c.parent_id as "pId", c.name, c.type, font_icon as "fontIcon", use_font_icon as "useFontIcon", icon_data as "iconData", true as "open", c.sort, false as checked
   	from owner_org c
   	where c.parent_id is null
		and exists(select from owner_org b where b.parent_id = c.id and b.type=1000 
			and exists(select from owner_org d where d.parent_id = b.id and d.type=100
				and exists (select from menu_org where org_id = d.id and menu_id=_menu_id)))
   	union
   	select oo.id, oo.parent_id, oo.name, oo.type, oo.font_icon as "fontIcon", oo.use_font_icon as "useFontIcon", oo.icon_data as "iconData", true as "open", oo.sort, true as checked
   	from owner_org oo
   	inner join org o ON o.id = oo.parent_id
   	where oo.deleted_by is null and disabled=false
		and (
			((oo.type=1000) and exists(select from owner_org where parent_id=oo.id and id in (select org_id from menu_org where menu_id = _menu_id)))
 			or 
			((oo.type = 100) and (oo.id in (select org_id from menu_org where menu_id = _menu_id)))
		)
)
select coalesce(json_agg(t), '[]')::text 
from(
	select *
	from org
	order by sort
) as t;
$$
language sql;



-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get Available department tree ready for assignment menu
-- Params:
--  _menu_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_available_department_tree_for_menu(_menu_id bigint)
returns text as $$
with recursive org as (
   	select c.id, c.parent_id as "pId", c.name, c.type, font_icon as "fontIcon", use_font_icon as "useFontIcon", icon_data as "iconData", true as "open", c.sort
   	from owner_org c
   	where c.parent_id is null
		and exists(select from owner_org b where b.parent_id = c.id and b.type=1000 
			and exists(select from owner_org d where d.parent_id = b.id and d.type=100
				and not exists (select from menu_org where org_id = d.id and menu_id=_menu_id)))
   	union
   	select oo.id, oo.parent_id, oo.name, oo.type, oo.font_icon as "fontIcon", oo.use_font_icon as "useFontIcon", oo.icon_data as "iconData", true as "open", oo.sort
   	from owner_org oo
   	inner join org o ON o.id = oo.parent_id
   	where oo.deleted_by is null and disabled=false
		and (
			((oo.type=1000) and exists(select from owner_org where parent_id=oo.id and id not in (select org_id from menu_org where menu_id = _menu_id)))
 			or 
			((oo.type = 100) and (oo.id not in (select org_id from menu_org where menu_id = _menu_id)))
		)
)
select coalesce(json_agg(t), '[]')::text 
from(
	select *
	from org
	order by sort
) as t;
$$
language sql;



-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get owner org with role tree
-- Params:
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_owner_org_role_tree(_include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
	with recursive org as (
		select id, name, parent_id, sort, type
		from owner_org
		where id in (select owner_org_id from role where ' || get_deleted_cond_str(null, _include_deleted) || ' and '|| get_disabled_cond_str(null, _include_disabled) ||')
		union
		select oo.id, oo.name, oo.parent_id, oo.sort, oo.type
		from owner_org oo
		inner join org on org.parent_id=oo.id
	)
	select ''org'' || o.id as id, o.name, ''org'' || o.parent_id as "pId", o.type, o.sort, true as "open", false as "done"
	from org o
	union
	select ''role'' || r.id, r.name, ''org'' || o.id as "pId", null as type, r.sort, true as "open", EXISTS(select id from role_detail where role_id=r.id limit 1) as "done"
	from role r
	inner join org o on o.id = r.owner_org_id
	order by type, sort, name
) as t';

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get assigned role department list by user id
-- Params:
--  _user_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_roled_department_list_by_user_id(_user_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin

if is_system_admin_by_user_id(_user_id, false, false) = true then
_query = 'select coalesce(json_agg(t), ''[]'')::text 
from(
select distinct dep.id as "departmentId",
	dep.name as "departmentName",
	dep.font_icon as "depFontIcon",
	dep.use_font_icon as "depUseFontIcon",
	dep.icon_data as "depIconData",
	dep.sort ,
	dep.created_date
from owner_org dep
where type = 100
	and ' || get_deleted_cond_str('dep', _include_deleted) || '
	and ' || get_disabled_cond_str('dep', _include_disabled) || '
	order by dep.sort, dep.name, dep.created_date
) as t';
else 
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
select distinct dep.id as "departmentId",
	dep.name as "departmentName",
	dep.font_icon as "depFontIcon",
	dep.use_font_icon as "depUseFontIcon",
	dep.icon_data as "depIconData",
	dep.sort ,
	dep.created_date
from owner_org dep
inner join menu_org mo on mo.org_id = dep.id and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '	
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '	
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '	
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '	
where ' || get_deleted_cond_str('dep', _include_deleted) || '
	and ' || get_disabled_cond_str('dep', _include_disabled) || '		
	order by dep.sort, dep.name, dep.created_date
) as t', _user_id);
end if;

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;

-- Module: System (sys)
-- Section: Owner Org (ono)
-- Function Description: Get the first assigned role department id by user id
-- Params:
--  _user_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_first_roled_department_id_by_user_id(_user_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('
select array_to_string(array(select dep.id
from owner_org dep
inner join menu_org mo on mo.org_id = dep.id and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '	
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '	
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '	
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '	
where ' || get_deleted_cond_str('dep', _include_deleted) || '
	and ' || get_disabled_cond_str('dep', _include_disabled) || '		
	order by dep.sort, dep.name, dep.created_date
	limit 1
), %L)', _user_id, ',');

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function sys_is_department_assigned_for_user(_dep_id bigint, _user_id bigint, _include_deleted bool, _include_disabled bool)
returns bool as $$
declare 
	_query TEXT;
	ret_val bool;
begin
if is_system_admin_by_user_id(_user_id, false, false) = true then
	ret_val = true;
else
_query = format('
select exists(select dep.id
from owner_org dep
inner join menu_org mo on mo.org_id = dep.id and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '	
inner join role_detail rd on rd.menu_org_id = mo.id and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '	
inner join role r on r.id = rd.role_id and ' || get_deleted_cond_str('r', _include_deleted) || '
	and ' || get_disabled_cond_str('r', _include_disabled) || '	
inner join assignment_role ar on ar.role_id = r.id and ar.user_id = %L and ' || get_deleted_cond_str('ar', _include_deleted) || '
	and ' || get_disabled_cond_str('ar', _include_disabled) || '	
where dep.id = %L and ' || get_deleted_cond_str('dep', _include_deleted) || '
	and ' || get_disabled_cond_str('dep', _include_disabled) || '		
	limit 1
)', _user_id, _dep_id);

execute _query into ret_val;
end if;
return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function sys_get_first_department_id_by_company_id(_company_id bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('
	select array_to_string(array(select d.id
	from owner_org d
	inner join owner_org b on b.id = d.parent_id and b.parent_id = %L
	where d.deleted_by is null and d.disabled = false
	limit 1
), %L)', _company_id, ',');

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;


create or replace function sys_get_human_org_tree(_user_id bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	with recursive org as (
		select c.id, c.parent_id as "pId", c.name, c.type, font_icon as "fontIcon", use_font_icon as "useFontIcon", icon_data as "iconData", true as "open", c.sort
		from owner_org c
		where c.parent_id is null 
		union
		select oo.id, oo.parent_id, oo.name, oo.type, oo.font_icon as "fontIcon", oo.use_font_icon as "useFontIcon", oo.icon_data as "iconData", true as "open", oo.sort 
		from owner_org oo
		inner join org o ON o.id = oo.parent_id
		where oo.deleted_by is null and disabled=false

	)
	select o.*, case when ho.org_id is not null then true else false end as "checked"
	from org o
	left join human_org ho on ho.org_id = o.id and ho.human_id = %L
	order by o.sort, o.name
) as t', _user_id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;



create or replace function sys_get_assigned_human_org_tree(_user_id bigint)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(with recursive org as (
		select c.id, c.parent_id as "pId", c.name, c.type, font_icon as "fontIcon", use_font_icon as "useFontIcon", icon_data as "iconData", true as "open", c.sort
		from owner_org c
		where c.parent_id is null
			and exists(select from owner_org b where b.parent_id = c.id and b.type=1000 
				and exists(select from owner_org d where d.parent_id = b.id and d.type=100
					and exists (select from human_org where org_id = d.id and human_id=%L)))
		union
		select oo.id, oo.parent_id, oo.name, oo.type, oo.font_icon as "fontIcon", oo.use_font_icon as "useFontIcon", oo.icon_data as "iconData", true as "open", oo.sort
		from owner_org oo
		inner join org o ON o.id = oo.parent_id
		where oo.deleted_by is null and disabled=false
			and (
				((oo.type=1000) and exists(select from owner_org where parent_id=oo.id and id in (select org_id from human_org where human_id = %L)))
				or 
				((oo.type = 100) and (oo.id in (select org_id from human_org where human_id = %L)))
		)
	)
	select o.*
	from org o
	order by o.sort, o.name
) as t', _user_id, _user_id, _user_id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;