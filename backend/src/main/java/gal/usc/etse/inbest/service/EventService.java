package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.repository.EventRepository;

public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {this.eventRepository = eventRepository;}

}
