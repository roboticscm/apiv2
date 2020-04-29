package vn.com.sky.sys.model;

import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class News extends GenericEntity {

	private String title;
	private String subTitle;
	private String intro;
	private String content;
	private String thumbnail;
	private Long startDate;
	private Long endDate;
	private Boolean pinOnTop = false;
	private String source;
	private Long viewCounter = 0L;

}