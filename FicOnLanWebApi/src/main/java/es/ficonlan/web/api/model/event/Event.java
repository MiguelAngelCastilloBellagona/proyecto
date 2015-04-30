package es.ficonlan.web.api.model.event;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Entity
@Table(name = "Event")
public class Event {

	@Column(name = "Event_id")
	@SequenceGenerator(name = "eventIdGenerator", sequenceName = "eventSeq")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "eventIdGenerator")
	private int eventId;

	@Column(name = "Event_name")
	private String name;

	@Column(name = "Event_description")
	private String description;

	@Column(name = "Event_num_participants")
	private int numParticipants;

	@Column(name = "Event_minimunAge")
	private int minimunAge;

	@Column(name = "Event_price")
	private int price;

	@Column(name = "Event_reg_date_open")
	private Calendar registrationOpenDate;

	@Column(name = "Event_reg_date_close")
	private Calendar registrationCloseDate;

	@Column(name = "Event_rules")
	private String normas;

	public Event() {
	}

	public Event(int eventId, String name, String description, int numParticipants, Calendar registrationOpenDate,
			Calendar registrationCloseDate) {
		this.eventId = eventId;
		this.name = name;
		this.description = description;
		this.numParticipants = numParticipants;
		this.minimunAge = 0;
		this.registrationOpenDate = registrationOpenDate;
		this.registrationCloseDate = registrationCloseDate;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getNumParticipants() {
		return numParticipants;
	}

	public void setNumParticipants(int numParticipants) {
		this.numParticipants = numParticipants;
	}

	public int getMinimunAge() {
		return minimunAge;
	}

	public void setMinimunAge(int minimunAge) {
		this.minimunAge = minimunAge;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	public Calendar getRegistrationOpenDate() {
		return registrationOpenDate;
	}

	public void setRegistrationOpenDate(Calendar registrationOpenDate) {
		this.registrationOpenDate = registrationOpenDate;
	}

	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	public Calendar getRegistrationCloseDate() {
		return registrationCloseDate;
	}

	public void setRegistrationCloseDate(Calendar registrationCloseDate) {
		this.registrationCloseDate = registrationCloseDate;
	}

	public String getNormas() {
		return normas;
	}

	public void setNormas(String normas) {
		this.normas = normas;
	}

}
