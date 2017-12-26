package com.haulmont.reports.testsupport.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "test$TimeEntry")
@Table(name = "test_time_entry")
@NamePattern("%s|date")
public class TimeEntry extends BaseUuidEntity {

    @Column(name = "time_in_minutes")
    private Long timeInMinutes = 0l;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_")
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getTimeInMinutes() {
        return timeInMinutes;
    }

    public void setTimeInMinutes(Long timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }
}
