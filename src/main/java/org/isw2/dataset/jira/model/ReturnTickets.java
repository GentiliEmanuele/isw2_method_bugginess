package org.isw2.dataset.jira.model;

import java.util.List;

public record ReturnTickets(List<Ticket> tickets, List<Ticket> toBeCorrected) {

}
