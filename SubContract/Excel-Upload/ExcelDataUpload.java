package backing;

import Utils.ADFUtils;

import Utils.JSFUtils;

import com.sun.el.parser.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.myfaces.trinidad.model.CollectionModel;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;


import oracle.adf.model.binding.DCIteratorBinding;

import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ViewObjectImpl;



public class ExcelDataUpload {
    public ExcelDataUpload() {
        super();
    }
    
    ViewObject excelTaskVO= ADFUtils.findIterator("excelValidation_ProjectTaskROVO1Iterator").getViewObject();
    ViewObject excelPRVO=ADFUtils.findIterator("excelValidation_PRLineROVO1Iterator").getViewObject();
    ViewObject excelUOMVO= ADFUtils.findIterator("excelValidation_UOMROVO1Iterator").getViewObject();
    

    
    public void readExcelSheet(InputStream xlsx, RichTable t1, Object ProjectId, Object prHID, Object prLineNumber) throws IOException,InvalidFormatException,ParseException {
        org.apache.poi.ss.usermodel.Workbook workbook = null;
        int sheetIndex = 0;  
        if (sheetIndex == 0) {
            CollectionModel cModel = (CollectionModel)t1.getValue();
            JUCtrlHierBinding tableBinding = (JUCtrlHierBinding)cModel.getWrappedData();        
            DCIteratorBinding iter = tableBinding.getDCIteratorBinding();
            try {  
                   workbook = WorkbookFactory.create(xlsx);
                 } catch (Exception e) {
                        System.err.println("Exception in Line Workbook : " + e);
                 }
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(sheetIndex);
            Integer skipRw = 1;
            Integer skipcnt = 1;
            int columnCount = 0;
            
            //Iterate over excel rows
            for (org.apache.poi.ss.usermodel.Row tempRow : sheet) {
                if (skipcnt == 1) {
                      columnCount = tempRow.getPhysicalNumberOfCells();
                  }
                if (skipcnt > skipRw) { //skip first row and start from 2 row .
                
                //Create new row in table
                    ADFUtils.findOperation("CreateInsert").execute();
                    System.err.println("New Line Row Added");
                    oracle.jbo.Row linerow =iter.getNavigatableRowIterator().getCurrentRow();            
                
                int Index = 0; //Iterate over row's columns
                for (int column = 0; column < columnCount; column++) {
                Cell MytempCell = tempRow.getCell(column);
                    if (MytempCell != null) {
                            Index = MytempCell.getColumnIndex();
                       } else {
                             //Index++;
                             }
                    try {
                        if (Index == 0) {
                                   if (MytempCell != null) 
                                   {
                                        System.err.println("Index 0--Task_Name" + MytempCell.getStringCellValue());
                                        ViewObjectImpl vo =(ViewObjectImpl)excelTaskVO;
                                        ViewCriteria vc =vo.getViewCriteria("taskNameValidateExcel");
                                        vo.applyViewCriteria(vc);
                                        vo.setNamedWhereClauseParam("BV_TNAME",MytempCell.getStringCellValue());
                                        vo.setNamedWhereClauseParam("BV_PID",ProjectId);
                                        vo.executeQuery();
                                        System.err.println("COUNT--TASK--" + vo.getEstimatedRowCount());
                                            if (vo.getEstimatedRowCount() == 1) 
                                            {
                                                String taskid = vo.first().getAttribute("ProjElementId") ==null ? "0" :
                                                                vo.first().getAttribute("ProjElementId").toString();
                                                String taskNUM =vo.first().getAttribute("ElementNumber") ==null ? "0" :
                                                                vo.first().getAttribute("ElementNumber").toString();
                                                System.err.println("Task ID" + taskid +"=Task Number=" +taskNUM);
                                                linerow.setAttribute("TaskId", taskid);
                                                linerow.setAttribute("TaskNumber",taskNUM);
                                             } 
                                             else {
                                                    JSFUtils.addFacesErrorMessage("Error in " +MytempCell.getStringCellValue());
                                                  }
                                      } else{
                                               System.err.println("!!!!!!!!!Task Name is blank!!!!!!!!!");
                                             }
                           } else if (Index == 1) {
                                    if (MytempCell != null) {
                                        System.err.println("Index 1==Requisition Number==" +MytempCell.getStringCellValue());
                                            if (MytempCell.getStringCellValue().equals(prLineNumber)) {
                                                linerow.setAttribute("ReqNumber",prLineNumber);
                                                } else {
                                                        JSFUtils.addFacesErrorMessage("Error in Requisition Number. Please check" +MytempCell.getStringCellValue());
                                                        }
                                                } else {
                                                        JSFUtils.addFacesErrorMessage("Error in " +MytempCell.getNumericCellValue());
                                                        }
                            } else if (Index == 2) {
                                       if (MytempCell != null) {
                                            System.err.println("Index 2-PR LINE " +MytempCell.getStringCellValue());
                                            ViewObjectImpl vo =(ViewObjectImpl)excelPRVO;
                                            ViewCriteria vc =vo.getViewCriteria("prLineValidateExcel");
                                            vo.applyViewCriteria(vc);
                                            vo.setNamedWhereClauseParam("BV_PRHID",prHID);
                                            vo.setNamedWhereClauseParam("BV_LINNUM",MytempCell.getStringCellValue());
                                            vo.executeQuery();
                                            System.err.println("COUNT--PR--" +vo.getEstimatedRowCount());
                                                if (vo.getEstimatedRowCount() == 1) {
                                                  String prLid =vo.first().getAttribute("RequisitionLineId") ==null ? "0" :vo.getCurrentRow().getAttribute("RequisitionLineId").toString();
                                                  String prLNum =vo.first().getAttribute("LineNumber") ==null ? "0" :vo.getCurrentRow().getAttribute("LineNumber").toString();
                                                  System.err.println("RequisitionLineId" +prLid +"=ReqLineNumber=" +prLNum);
                                                                linerow.setAttribute("ReqLineId",prLid);
                                                                linerow.setAttribute("ReqLineNumber",prLNum);
                                                 } else {
                                                                JSFUtils.addFacesErrorMessage("Error in " +MytempCell.getStringCellValue());
                                                        }
                                                 } else {
                                                            System.err.println("!!!!!!!!!ReqLineNumber is blank!!!!!!!!!");
                                                        }
                             } else if (Index == 3) {
                                          if (MytempCell != null) {
                                           System.err.println("Index 3--PR ItemDescription " +MytempCell.getStringCellValue());
                                           ViewObjectImpl vo =(ViewObjectImpl)excelPRVO;
                                           ViewCriteria vc =vo.getViewCriteria("excelValidation_PRLineDesc");
                                           vo.applyViewCriteria(vc);
                                           vo.setNamedWhereClauseParam("BV_PRHID",prHID);
                                           vo.setNamedWhereClauseParam("BV_DESC",MytempCell.getStringCellValue());
                                           vo.executeQuery();
                                           System.err.println("COUNT--PR2--" +vo.getEstimatedRowCount());
                                                 if (vo.getEstimatedRowCount() == 1) {
                                                       String prDescribe =vo.first().getAttribute("ItemDescription") ==null ? "0" :
                                                                           vo.getCurrentRow().getAttribute("ItemDescription").toString();
                                                     System.err.println("==Describtion===" +prDescribe);
                                                     linerow.setAttribute("ItemDescription",prDescribe);
                                                } else {
                                                JSFUtils.addFacesErrorMessage("Error in " +MytempCell.getStringCellValue());
                                                     }
                                                } else {
                                                            System.err.println("!!!!!!!!!Line Number is blank!!!!!!!!!");
                                                        }
                               } else if (Index == 4) {
                                          if (MytempCell != null) {
                                          System.err.println("Index 4--BOQ Item Description--" +MytempCell.getStringCellValue());
                                            linerow.setAttribute("BoqItemDesc",MytempCell.getStringCellValue());
                                           } else {
                                                     System.err.println("!!!!!!!!!BoqItemDesc is blank!!!!!!!!!");
                                                  }
                                } else if (Index == 5) {
                                         if (MytempCell != null) {
                                         System.err.println("Index 5 " +MytempCell.getStringCellValue());
                                             ViewObjectImpl vo =(ViewObjectImpl)excelUOMVO;
                                             ViewCriteria vc =vo.getViewCriteria("UOMValidateExcel");
                                             vo.applyViewCriteria(vc);
                                             vo.setNamedWhereClauseParam("BV_DES",MytempCell.getStringCellValue());
                                             vo.executeQuery();
                                                if (vo.getEstimatedRowCount() == 1) {
                                                Object uomDESC =vo.first().getAttribute("Description") ==null ? 0 :
                                                                vo.getCurrentRow().getAttribute("Description").toString();
                                                Object uomCODE =vo.first().getAttribute("UomCode") ==null ? 0 :
                                                                vo.getCurrentRow().getAttribute("UomCode").toString();
                                                System.err.println("=Description=" +uomDESC +"=UomCode=" +uomCODE);
                                                linerow.setAttribute("Uom", uomCODE);
                                                } else {
                                               JSFUtils.addFacesErrorMessage("Error in " +MytempCell.getStringCellValue());
                                                  }
                                                        } else {
                                                            System.err.println("!!!!!!!!!UOM is blank!!!!!!!!!");
                                                        }
                                  } else if (Index == 6) {
                                          if (MytempCell != null) {
                                                     System.err.println("Index 6--QTY " +MytempCell.getNumericCellValue());
                                                            linerow.setAttribute("OrigQuantity",MytempCell.getNumericCellValue());
                                                        } else {
                                                            System.err.println("!!!!!!!!!QTY is blank!!!!!!!!!");
                                                        }
                                  } else if (Index == 7) {
                                        if (MytempCell != null) {
                                                System.err.println("Index 7--PRIZE " +MytempCell.getNumericCellValue());
                                                 linerow.setAttribute("OrigUnitPrice",MytempCell.getNumericCellValue());
                                                 float qty =linerow.getAttribute("OrigQuantity") ==null ? 0 :
                                                                Float.parseFloat(linerow.getAttribute("OrigQuantity").toString());
                                                System.err.println("--qty--" + qty);
                                                 float prz =linerow.getAttribute("OrigUnitPrice") ==null ? 0 :
                                                                Float.parseFloat(linerow.getAttribute("OrigUnitPrice").toString());
                                                  System.err.println("--prz--" + prz);
                                                  float amt = qty * prz;
                                                 System.err.println("--amt--" + amt);
                                                  linerow.setAttribute("OrigAmount", amt);
                                                        } else {
                                                            System.err.println("!!!!!!!!!Prize is blank!!!!!!!!!");
                                                        }
                                                    } 
                    }catch(Exception e){
                        System.err.println("Exception Occured at Line & column position is.... " +Index);
                        e.printStackTrace();
                    }

                } // one row completed 
                
            } ////skip first n row for labels.
            skipcnt++;
          }
            AdfFacesContext.getCurrentInstance().addPartialTarget(t1);    
            JSFUtils.addFacesInformationMessage("Line Added Successfully");
            
        }
    }
    
    
    

    
        
}
