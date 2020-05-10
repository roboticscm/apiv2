create or replace function tsk_find_tasks(_user_id bigint, _menu_path text, _dep_id bigint, _page bigint, _page_size bigint)
returns text as $$
declare 
	_query text;
	ret_val text;
	data_level_cond text;
	list_staff text;
begin
list_staff = get_list_staff_ids(_user_id, _menu_path, _dep_id);
if list_staff is null then -- full control
	data_level_cond = ' and true ';
elseif list_staff = '' then -- prevent access
	data_level_cond = ' and false ';
else
	data_level_cond = ' and t.created_by in (' || list_staff || ')';
end if;

_query = format('
	select t.*,
		project.name as "projectName",
		coalesce(t.updated_date, t.created_date) as updated_or_created_date
	from tsk_task t
	left join tsk_project project on project.id = t.project_id
	where t.deleted_by is null %s
	order by updated_or_created_date
', data_level_cond);


return  paginate(_query, _page, _page_size);
end;
$$ language plpgsql called on null input;



create or replace function tsk_get_task_by_id(_id bigint)
returns text as $$
declare 
	_query text;
	ret_val text;
begin
_query = format('select coalesce(json_agg(t), ''[]'')::text 
from(
	select t.*, 
		array_agg(distinct taf.file_name) as "taskAttachFiles",
		array_agg(distinct jsonb_build_object(''id'', assigner.human_or_org_id, ''name'', h_assigner.last_name || '' '' || h_assigner.first_name)) as "assigners",
		array_agg(distinct jsonb_build_object(''id'', assignee.human_or_org_id, ''name'', h_assignee.last_name || '' '' || h_assignee.first_name)) as "assignees",
		array_agg(distinct jsonb_build_object(''id'', evaluator.human_or_org_id, ''name'', h_evaluator.last_name || '' '' || h_evaluator.first_name)) as "evaluators",
		array_agg(distinct jsonb_build_object(''id'', char.owner_org_id, ''name'', o_char.name)) as "chars",
		array_agg(distinct jsonb_build_object(''id'', target_person.human_or_org_id, ''name'', h_target_person.last_name || '' '' || h_target_person.first_name)) as "targetPersons",
		array_agg(distinct jsonb_build_object(''id'', target_team.owner_org_id, ''name'', o_target_team.name)) as "targetTeams",
		array_agg(jsonb_build_object(''index'', assigner_status_detail.id, ''id'', assigner_status_detail.id, ''taskId'', assigner_status_detail.task_id, ''statusId'', assigner_status.id, ''status'', assigner_status.name, ''startTime'', assigner_status_detail.start_time, ''endTime'', assigner_status_detail.end_time, ''note'', assigner_status_detail.note,
				''attachFiles'', (select array_agg(file_name) from tsk_status_attach_file where status_detail_id=assigner_status_detail.id)
				
		) order by assigner_status_detail.note desc) as "assignerStatusDetails"
	from tsk_task t
				
	left join tsk_task_attach_file taf on taf.task_id = t.id
				
	left join tsk_assign_human_or_org assigner on assigner.task_id = t.id and assigner.assign_position=%L
	left join human_or_org h_assigner on h_assigner.id = assigner.human_or_org_id
			
	left join tsk_assign_human_or_org assignee on assignee.task_id = t.id and assignee.assign_position=%L
	left join human_or_org h_assignee on h_assignee.id = assignee.human_or_org_id
				
	left join tsk_assign_human_or_org evaluator on evaluator.task_id = t.id and evaluator.assign_position=%L
	left join human_or_org h_evaluator on h_evaluator.id = evaluator.human_or_org_id
				
	left join tsk_assign_owner_org char on char.task_id = t.id and char.assign_position=%L
	left join owner_org o_char on o_char.id = char.owner_org_id
				
	left join tsk_assign_human_or_org target_person on target_person.task_id = t.id and target_person.assign_position=%L
	left join human_or_org h_target_person on h_target_person.id = target_person.human_or_org_id
	
	left join tsk_assign_owner_org target_team on target_team.task_id = t.id and target_team.assign_position=%L
	left join owner_org o_target_team on o_target_team.id = target_team.owner_org_id
				
	left join tsk_status_detail assigner_status_detail on assigner_status_detail.task_id = t.id and assigner_status_detail.assign_position=%L
	left join tsk_status assigner_status on assigner_status.id = assigner_status_detail.status_id
				
	where t.id = %L
	group by t.id
) as t', 'ASSIGNER', 'ASSIGNEE', 'EVALUATOR', 'CHAR', 'TARGET_PERSON', 'TARGET_TEAM', 'ASSIGNER', _id );

execute _query into ret_val;
return  ret_val;
end;
$$ language plpgsql called on null input;
