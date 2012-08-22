' Create excel object
Set objXL = CreateObject("Excel.Application") 
' Show me everything
objXL.Visible=True

' Open the personal workbook containing system wide macros (make accessible)
Set wbPersonal = objXL.Workbooks.Open("C:\Users\n06038\AppData\Roaming\Microsoft\Excel\XLSTART\PERSONAL.XLSB")

' Open the (central) combined data file
Set wbCombined = objXL.WorkBooks.Open("C:\Users\n06038\Desktop\logs\800rpm-7st-var(CR).xlsx")
Set wsCombined = wbCombined.WorkSheets(1)


''''''''''' ALL '''''''''''''

' Select the starting position
startRow = 5
wsCombined.Cells(startRow,1).Select
i = 1

For rpm = 2 to 8
	wsCombined.Cells(startRow-2,i+1+(i-1)*3).Value = rpm & " Change Position"
	For run = 0 to 2
		wsCombined.Cells(startRow-1,i+1+(i-1)*3+run).Value = "Run " & run
		' Open the log file
		Set wbLog = objXL.WorkBooks.Open("C:\Users\n06038\Desktop\logs\800rpm-7st-var(CR)\" & rpm & "CRpos-ALL-run" & run & "\CRPlacementV1-Dist-END.log")
		' Run the macro to calculate the throughput
		wbLog.Application.Run "PERSONAL.XLSB!s5SuspensionThroughput35"
		' Copy the cells
		wbLog.Application.Run "PERSONAL.XLSB!copyData35"
		wbLog.Close False
		
		' Paste the data
		wbCombined.Application.Run "PERSONAL.XLSB!pasteData35"
	Next
	' Average
	wbCombined.Application.Run "PERSONAL.XLSB!average35"
	i=i+1
Next



''''''''''' CR '''''''''''''

' Select the starting position
startRow = 49
wsCombined.Cells(startRow,1).Select
i=1
For rpm = 2 to 8
	wsCombined.Cells(startRow-2,i+1+(i-1)*3).Value = rpm & " Change Position"
	For run = 0 to 2
		wsCombined.Cells(startRow-1,i+1+(i-1)*3+run).Value = "Run " & run
		' Open the log file
		Set wbLog = objXL.WorkBooks.Open("C:\Users\n06038\Desktop\logs\800rpm-7st-var(CR)\" & rpm & "CRpos-CR-run" & run & "\CRPlacementV1-Dist-END.log")
		' Run the macro to calculate the throughput
		wbLog.Application.Run "PERSONAL.XLSB!s5SuspensionThroughput35"
		' Copy the cells
		wbLog.Application.Run "PERSONAL.XLSB!copyData35"
		wbLog.Close False
		
		' Paste the data
		wbCombined.Application.Run "PERSONAL.XLSB!pasteData35"
	Next
	' Average
	wbCombined.Application.Run "PERSONAL.XLSB!average35"
	i=i+1
Next

' wbCombined.Save
' wbCombined.Close

