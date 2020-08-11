package com.example.actionaidactivista.models;

public class ReportedComment {
    private String id;
    private String reporterid;
    private String offenderid;
    private String commentid;
    private String rpttype;
    private String reason;
    private String comment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterid() {
        return reporterid;
    }

    public void setReporterid(String reporterid) {
        this.reporterid = reporterid;
    }

    public String getOffenderid() {
        return offenderid;
    }

    public void setOffenderid(String offenderid) {
        this.offenderid = offenderid;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public String getRpttype() {
        return rpttype;
    }

    public void setRpttype(String rpttype) {
        this.rpttype = rpttype;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
