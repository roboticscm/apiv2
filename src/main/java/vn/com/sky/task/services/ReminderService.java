package vn.com.sky.task.services;

import java.util.List;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.NotifyType;
import vn.com.sky.sys.model.PartNotification;
import vn.com.sky.sys.notification.NotificationRepo;
import vn.com.sky.sys.ownerorg.CustomOwnerOrgRepo;
import vn.com.sky.task.model.TskTask;
import vn.com.sky.task.task.TskAssignHumanOrOrgRepo;
import vn.com.sky.task.task.TskTaskRepo;
import vn.com.sky.util.SDate;

@Configuration
@AllArgsConstructor
public class ReminderService extends GenericREST implements Runnable {
	private TskTaskRepo taskRepo;
	private TskAssignHumanOrOrgRepo assignHumanRepo;
	private NotificationRepo notificationRepo;
	private CustomOwnerOrgRepo ownerOrgRepo;
	
	private static final int CHECK_TIME = 5*1000;
	@Override
	public void run() {
		while(true) {
			long now = SDate.now();
			
			
			var firstReminder = taskRepo.findFirstReminders(now - 2*CHECK_TIME, now + 2*CHECK_TIME).collectList().block();
			if(firstReminder != null) {
				firstReminder.forEach(item -> {
					var humanIds = assignHumanRepo.findHumanIdsByTaskId(item.getId()).collectList().block();
					item.setIsFirstRemindered(true);
					updateEntity(taskRepo, item, null).subscribe();
					
					notify(item, humanIds, "First Reminder");
				});
			}
			
			
			var secondReminder = taskRepo.findSecondReminders(now - 2*CHECK_TIME, now + 2*CHECK_TIME).collectList().block();
			if(secondReminder != null) {
				secondReminder.forEach(item -> {
					var humanIds = assignHumanRepo.findHumanIdsByTaskId(item.getId()).collectList().block();
					item.setIsSecondRemindered(true);
					updateEntity(taskRepo, item, null).subscribe();
					
					notify(item, humanIds, "Second Reminder");
				});
			}
			
			try {
				Thread.sleep(CHECK_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	private void notify(TskTask task, List<Long> humanIds, String reminderType) {
		humanIds.forEach(humanId -> {
			final var menuPath = "task/task";
			
			Long depId = null;
			try {
				depId = ownerOrgRepo.sysGetFirstRoledDepId(humanId, menuPath).block();
			}catch(Exception e) {
				
			}			

			var notification = new PartNotification();
			notification.setDepartmentId(depId);
			notification.setToHumanId(humanId);
			notification.setMenuPath(menuPath);
			notification.setTargetId(task.getId());
			notification.setType(NotifyType.ALARM.toString());
			notification.setTitle(reminderType + ": " + task.getName());
			saveEntity(notificationRepo, notification, null).subscribe();
		});
	}

}
