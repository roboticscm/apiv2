
-- Module: System (sys)
-- Section: Role Control (rct)
-- Function Description: Get Roled control list by department id and user id and menu path
-- Params:
--  _user_id
--  _menu_path
create or replace function sys_get_control_list_by_dep_id_and_user_id_and_menu_path(_dep_id bigint, _user_id bigint, _menu_path text)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
if is_system_admin_by_user_id(_user_id, false, false) = true then
	_query = 'select coalesce(to_json(t), ''[]'')::text 
	from(
		select true as "fullControl"
	) as t';
else
	_query = format('select coalesce(json_agg(t), ''[]'')::text 
	from(
	select distinct c.id as "controlId",
		c.code as "controlCode",
		rc.render_control as "renderControl",
		rc.disable_control as "disableControl",
		rc.confirm as "confirm",
		rc.require_password as "requirePassword"
	from control c
	inner join menu_control mc on mc.control_id = c.id and ' || get_deleted_cond_str('mc', false) || '
			and ' || get_disabled_cond_str('mc', false) || '
	inner join role_control rc on rc.menu_control_id = mc.id and ' || get_deleted_cond_str('rc', false) || '
			and ' || get_disabled_cond_str('rc', false) || '
	inner join menu m on m.id = mc.menu_id and m.path=%L and ' || get_deleted_cond_str('m', false) || '
			and ' || get_disabled_cond_str('m', false) || '
	inner join role_detail rd on rd.id = rc.role_detail_id and ' || get_deleted_cond_str('rd', false) || '
			and ' || get_disabled_cond_str('rd', false) || '
	inner join assignment_role ar on ar.role_id = rd.role_id and ar.user_id = %L and ' || get_deleted_cond_str('ar', false) || '
			and ' || get_disabled_cond_str('ar', false) || '
	inner join menu_org mo on mo.id = rd.menu_org_id and mo.menu_id = m.id and mo.org_id = %L 
			and ' || get_deleted_cond_str('mo', false) || '
			and ' || get_disabled_cond_str('mo', false) || '
	where
		' || get_deleted_cond_str('c', false) || '
		and ' || get_disabled_cond_str('c', false) || '
	) as t', _menu_path, _user_id, _dep_id);
end if;

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;