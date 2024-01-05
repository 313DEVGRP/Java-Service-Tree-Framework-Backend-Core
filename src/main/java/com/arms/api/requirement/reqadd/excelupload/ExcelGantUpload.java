package com.arms.api.requirement.reqadd.excelupload;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelGantUpload {

	private static int WBS_NAME_INDEX = 3;
	private static int HEADER_ROW = 4;
	private final Sheet sheet;
	private final List<WbsSchedule> getWebScheduleList;

	public ExcelGantUpload(InputStream inputStream) throws IOException {
		try(inputStream) {
			this.sheet = WorkbookFactory.create(inputStream).getSheet("Schedule");
		}
		this.getWebScheduleList = new ArrayList<>(convertExcelToWbsSchedule());

		getWebScheduleList.stream().collect(Collectors.toList());
	}

	private List<WbsSchedule> convertExcelToWbsSchedule() {
		return StreamSupport.stream(sheet.spliterator(), false)
			.skip(HEADER_ROW)
			.flatMap(this::rowToWbsSchedule)
			.collect(Collectors.toList());
	}

	private Stream<WbsSchedule> rowToWbsSchedule(Row row) {
		return IntStream.range(WBS_NAME_INDEX, row.getLastCellNum())
			.mapToObj(index -> row.getCell(index))
			.filter(cell -> cell.getColumnIndex() == WBS_NAME_INDEX && !cell.getStringCellValue().isEmpty())
			.map(cell -> {
				String wbsName = getValueFromCell(cell);
				Cell jobNameCell = row.getCell(cell.getColumnIndex() + getJobIndex(wbsName));
				return WbsSchedule.builder()
					.wbsName(wbsName)
					.jobName(getValueFromCell(jobNameCell))
					.depth(getJobIndex(wbsName))
					.build();
			})
			.filter(Objects::nonNull);
	}

	private int getJobIndex(String wbsName){
		return wbsName.split("\\.").length;
	}

	private String getValueFromCell(Cell cell) {
		switch (cell.getCellType()) {
			case STRING:
				return String.valueOf(cell.getStringCellValue());
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return String.valueOf(cell.getDateCellValue());
				}
				return String.valueOf(cell.getNumericCellValue());
			case FORMULA:
				return String.valueOf(cell.getCellFormula());
			case BLANK:
				return "";
			default:
				return "";
		}
	}

	public List<WbsSchedule> getGetWebScheduleList() {
		return getWebScheduleList;
	}
}
