
-- Module: System (sys)
-- Section: Role Detail (rdt)
-- Function Description: get menu and role control list by owner_org_id and role_id
-- Params:
--  _owner_org_id
--  _rold_id
--  _include_deleted: Include deleted record
--  _include_disabled: Include disabled record
create or replace function sys_get_menu_role_control_list(_owner_org_id bigint, _role_id bigint, _include_deleted bool, _include_disabled bool)
returns text as $$
declare 
	_query TEXT;
	ret_val TEXT;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
select bra.id as "branchId",
	bra.name as "branchName",
	dep.id as "departmentId",
	dep.name as "departmentName",
	m.id as "menuId",
	exists(select id from role_detail where role_id = %L and menu_org_id = mo.id limit 1) as  "checked",
	m.name as "menuName",
	coalesce(rd.is_private, false) as "isPrivate",
	coalesce(rd.data_level, 0) as "dataLevel",
	coalesce(rd.approve, false) as "approve",
	mc.control_id as "controlId",
	ct.name as "controlName",
 	rc.render_control as "renderControl",
 	coalesce(rc.disable_control, false) as "disableControl",
	coalesce(rc.confirm, ct.confirm) as "confirm",
	coalesce(rc.require_password, ct.require_password) as "requirePassword"
from menu m
left join menu_control mc on mc.menu_id = m.id
left join control ct on ct.id = mc.control_id
inner join menu_org mo on mo.menu_id = m.id
inner join owner_org dep on dep.id = mo.org_id and dep.id in (' || sys_get_sub_org_ids(_owner_org_id, _include_deleted, _include_disabled)  || ')
inner join owner_org bra on bra.id = dep.parent_id 
left join role_detail rd on rd.menu_org_id = mo.id and rd.role_id = %L 
left join role_control rc on rc.menu_control_id = mc.id and rc.role_detail_id = rd.id
where ' || get_deleted_cond_str('mc', _include_deleted) || '
	and ' || get_disabled_cond_str('mc', _include_disabled) || '
	and ' || get_deleted_cond_str('ct', _include_deleted) || '
	and ' || get_disabled_cond_str('ct', _include_disabled) || '	
	and ' || get_deleted_cond_str('m', _include_deleted) || '
	and ' || get_disabled_cond_str('m', _include_disabled) || '
	and ' || get_deleted_cond_str('mo', _include_deleted) || '
	and ' || get_disabled_cond_str('mo', _include_disabled) || '
	and ' || get_deleted_cond_str('dep', _include_deleted) || '
	and ' || get_disabled_cond_str('dep', _include_disabled) || '
	and ' || get_deleted_cond_str('bra', _include_deleted) || '
	and ' || get_disabled_cond_str('bra', _include_disabled) || '
	and ' || get_deleted_cond_str('rd', _include_deleted) || '
	and ' || get_disabled_cond_str('rd', _include_disabled) || '
	and ' || get_deleted_cond_str('rc', _include_deleted) || '
	and ' || get_disabled_cond_str('rc', _include_disabled) || '
	order by bra.sort, bra.name, bra.id, dep.sort, dep.name, dep.id, m.sort, m.name, m.id, ct.name
) as t', _role_id, _role_id);

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;