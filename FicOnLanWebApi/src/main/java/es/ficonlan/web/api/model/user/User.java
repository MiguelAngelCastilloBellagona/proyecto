package es.ficonlan.web.api.model.user;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Entity
@Table(name = "User")
public class User {

	@Column(name = "User_id")
    @SequenceGenerator(name = "userIdGenerator", sequenceName = "userSeq")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "userIdGenerator")
    private int userId;
	
    @Column(name = "User_permissions")
	private String premissions;

	@Column(name = "User_name")
    private String name;

    @Column(name = "User_login")
    private String login;

    @Column(name = "User_password")
    private String password;

    @Column(name = "User_secondPassword")
    private String secondPassword;
    
    @Column(name = "User_secondPasswordExpDate")
    private Calendar secondPasswordExpDate;

	@Column(name = "User_dni")
    private String dni;

    @Column(name = "User_email")
    private String email;

    @Column(name = "User_telf")
    private String phoneNumber;
    
    @Column(name = "User_shirtSize")
    private String shirtSize;

	@Column(name = "User_borndate")
    private Calendar dob;

	@Column(name = "User_language")
    private String language;
    
	public User() {}
	
	public User(String permissions, String name, String login, String password, String dni, String email, String phoneNumber, String shirtSize, Calendar dob, String language) {
        this.premissions = permissions;
		this.name = name;
        this.login = login;
        this.password = password;
        this.secondPassword = password;
        this.secondPasswordExpDate = Calendar.getInstance();
        this.dni = dni;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.shirtSize = shirtSize;
        this.dob = dob;
        this.language = language;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getPremissions() {
		return premissions;
	}

	public void setPremissions(String premissions) {
		this.premissions = premissions;
	}

    public String getName() {
        return name;
    }

	public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @JsonIgnore
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    @JsonProperty("seccondpassword")
    public String getSecondPassword() {
		return secondPassword;
	}

	public void setSecondPassword(String secondPassword) {
		this.secondPassword = secondPassword;
	}

    @JsonIgnore
	public Calendar getSecondPasswordExpDate() {
		return secondPasswordExpDate;
	}

	public void setSecondPasswordExpDate(Calendar secondPasswordExpDate) {
		this.secondPasswordExpDate = secondPasswordExpDate;
	}

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getShirtSize() {
        return this.shirtSize;
    }
    
    public void setShirtSize(String sirtSize) {
        this.shirtSize = sirtSize;
    }
    
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	public Calendar getDob() {
		return dob;
	}

	public void setDob(Calendar fechaNacimiento) {
		this.dob = fechaNacimiento;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
