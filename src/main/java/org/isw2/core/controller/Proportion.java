package org.isw2.core.controller;

import org.isw2.core.controller.context.ProportionContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Proportion implements Controller<ProportionContext, Void> {

    @Override
    public Void execute(ProportionContext context) throws ProcessingException {

        List<Ticket> consistent = new ArrayList<>(context.tickets());
        removeInconsistent(consistent);

        float p = computeWithComplete(consistent, context.versions());

        correct(context.tickets(), context.versions(), (int) Math.ceil(p));
        removeInconsistent(context.tickets());
        return null;
    }

    private float computeWithComplete(List<Ticket> tickets, List<Version> versions) {
        float p = 0.0F;
        int computed = 0;
        for (Ticket ticket : tickets) {
            ticket.getAffectedVersions().sort(Comparator.comparing(o -> LocalDate.parse(o.getReleaseDate())));
            if (versions.indexOf(ticket.getFixedVersion()) != versions.indexOf(ticket.getOpeningVersion())) {
                p += (float) (versions.indexOf(ticket.getFixedVersion()) - versions.indexOf(ticket.getInjectedVersion())) /
                        (versions.indexOf(ticket.getFixedVersion()) - versions.indexOf(ticket.getOpeningVersion()));
                computed++;
            }
        }
        return computed > 0 ? p / computed : 0;
    }

    private void correct(List<Ticket> tickets, List<Version> versions, int ceilP) {
        for (Ticket ticket : tickets) {
            // If the ticket has injected, opening or fixed null discard it and go to the next.
            if (
                    checkNotConsistencyZero(
                            ticket.getInjectedVersion(),
                            ticket.getOpeningVersion(),
                            ticket.getFixedVersion()
                    ) && ticket.getOpeningVersion() == null && (ticket.getInjectedVersion() != null && ticket.getFixedVersion() != null)
            ) {
                // If IV is null but AV e FV is not null, set OV = IV
                ticket.setOpeningVersion(ticket.getInjectedVersion());
                continue;
            }

            // If the ticket has OV > FV set OV = IV
            if (notOpeningBeforeFixed(ticket.getOpeningVersion(), ticket.getFixedVersion())) {
                ticket.setOpeningVersion(ticket.getInjectedVersion());
            }

            // If the ticket has IV > OV compute IV using p
            if (notInjectedBeforeOpening(ticket.getInjectedVersion(), ticket.getOpeningVersion())) {
                int fixedVersionIndex = versions.indexOf(ticket.getFixedVersion());
                int openingVersionIndex = versions.indexOf(ticket.getOpeningVersion());
                // IV = FV - (FV - OV) * P
                int injectedVersionIndex = Math.max(fixedVersionIndex - (fixedVersionIndex - openingVersionIndex) * ceilP, 0);
                ticket.setAffectedVersions(getNPreviousVersions(versions, injectedVersionIndex, fixedVersionIndex));
                ticket.setInjectedVersion(ticket.getAffectedVersions().getFirst());
            }
        }
    }

    private void removeInconsistent(List<Ticket> tickets) {
        tickets.removeIf(ticket ->
                checkNotConsistencyZero(
                        ticket.getInjectedVersion(),
                        ticket.getOpeningVersion(),
                        ticket.getFixedVersion()
                ) ||
                        checkNotConsistencyFirst(
                                ticket.getInjectedVersion(),
                                ticket.getOpeningVersion(),
                                ticket.getFixedVersion()
                        ) ||
                        checkNotConsistencySecond(
                                ticket.getInjectedVersion(),
                                ticket.getAffectedVersions(),
                                ticket.getFixedVersion()
                        )

        );
    }

    private boolean checkNotConsistencyZero(Version injectedVersion, Version openingVersion, Version fixedVersion) {
        return injectedVersion == null || openingVersion == null || fixedVersion == null;
    }

    private boolean checkNotConsistencyFirst(Version injectedVersion, Version openingVersion, Version fixedVersion) {
        // If IV <= OV return false
        if (notInjectedBeforeOpening(injectedVersion, openingVersion)) {
            return true;
        }

        // If OV <= FV return false
        return notOpeningBeforeFixed(openingVersion, fixedVersion);
    }

    private boolean checkNotConsistencySecond(Version injectedVersion, List<Version> affectedVersions, Version fixedVersion) {
        return !affectedVersions.getFirst().getReleaseDate().equals(injectedVersion.getReleaseDate()) || !affectedVersions.getLast().getReleaseDate().equals(fixedVersion.getReleaseDate());
    }

    private List<Version> getNPreviousVersions(List<Version> versions, int startIndex, int lastIndex) {
        List<Version> nPreviousVersions = new ArrayList<>();
        for (int i = startIndex; i <= lastIndex; i++) {
            nPreviousVersions.add(versions.get(i));
        }
        return nPreviousVersions;
    }

    private boolean notInjectedBeforeOpening(Version injectedVersion, Version openingVersion) {
        LocalDate injectedDate = LocalDate.parse(injectedVersion.getReleaseDate());
        LocalDate openingDate = LocalDate.parse(openingVersion.getReleaseDate());
        return !injectedDate.isBefore(openingDate) && !injectedDate.isEqual(openingDate);
    }

    private boolean notOpeningBeforeFixed(Version openingVersion, Version fixedVersion) {
        LocalDate openingDate = LocalDate.parse(openingVersion.getReleaseDate());
        LocalDate fixedDate = LocalDate.parse(fixedVersion.getReleaseDate());
        return !openingDate.isBefore(fixedDate) && !openingDate.isEqual(fixedDate);
    }
}
