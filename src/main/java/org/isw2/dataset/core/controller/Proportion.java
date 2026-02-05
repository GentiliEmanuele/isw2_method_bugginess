package org.isw2.dataset.core.controller;

import org.isw2.dataset.core.controller.context.ProportionContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.jira.model.Ticket;
import org.isw2.dataset.jira.model.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Proportion implements Controller<ProportionContext, Void> {

    @Override
    public Void execute(ProportionContext context) throws ProcessingException {

        float p = computeWithComplete(context.returnTickets().tickets(), context.versions());

        correct(context.returnTickets().toBeCorrected(), context.versions(), p);

        return null;
    }

    private float computeWithComplete(List<Ticket> tickets, List<Version> versions) {
        float p = 0.0F;
        int computed = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getAffectedVersions().isEmpty() || (ticket.getFixedVersion() == null || ticket.getOpeningVersion() == null || ticket.getInjectedVersion() == null)) continue;
            ticket.getAffectedVersions().sort(Comparator.comparing(o -> LocalDate.parse(o.getReleaseDate())));

            if (versions.indexOf(ticket.getFixedVersion()) != versions.indexOf(ticket.getOpeningVersion())) {
                p += (float) (versions.indexOf(ticket.getFixedVersion()) - versions.indexOf(ticket.getInjectedVersion())) /
                        (versions.indexOf(ticket.getFixedVersion()) - versions.indexOf(ticket.getOpeningVersion()));
                computed++;
            }
        }
        return computed > 0 ? p / computed : 0;
    }

    private void correct(List<Ticket> toBeCorrected, List<Version> versions, float p) {
        for (Ticket ticket : toBeCorrected) {
            int fixedVersionIndex = versions.indexOf(ticket.getFixedVersion());
            int openingVersionIndex = versions.indexOf(ticket.getOpeningVersion());
            // IV = FV - (FV - OV) * P
            float injectedVersionFloatIndex = fixedVersionIndex - (fixedVersionIndex - openingVersionIndex) * p;
            int injectedVersionIndex = Math.max(Math.round(injectedVersionFloatIndex), 0);
            ticket.setAffectedVersions(getNPreviousVersions(versions, injectedVersionIndex, fixedVersionIndex));
            ticket.setInjectedVersion(ticket.getAffectedVersions().getFirst());
        }
    }


    private List<Version> getNPreviousVersions(List<Version> versions, int startIndex, int lastIndex) {
        List<Version> nPreviousVersions = new ArrayList<>();
        for (int i = startIndex; i <= lastIndex; i++) {
            nPreviousVersions.add(versions.get(i));
        }
        return nPreviousVersions;
    }
}
