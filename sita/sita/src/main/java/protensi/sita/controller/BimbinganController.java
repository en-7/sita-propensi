package protensi.sita.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import protensi.sita.model.AvailableBimbinganModel;
import protensi.sita.model.EnumRole;
import protensi.sita.model.JadwalBimbinganModel;
import protensi.sita.model.MahasiswaModel;
import protensi.sita.model.PembimbingModel;
import protensi.sita.model.UgbModel;
import protensi.sita.model.UserModel;
import protensi.sita.security.UserDetailsServiceImpl;
import protensi.sita.service.AvailableBimbinganServiceImpl;
import protensi.sita.service.BaseService;
import protensi.sita.service.JadwalBimbinganServiceImpl;
import protensi.sita.service.MahasiswaServiceImpl;
import protensi.sita.service.PembimbingServiceImpl;
import protensi.sita.service.UgbServiceImpl;
import protensi.sita.repository.UserDb;


@Controller
@RequestMapping("/bimbingan")
public class BimbinganController {

    @Qualifier("availableBimbinganServiceImpl")
    @Autowired
    private AvailableBimbinganServiceImpl availableBimbinganService;

    @Qualifier("jadwalBimbinganServiceImpl")
    @Autowired
    private JadwalBimbinganServiceImpl jadwalBimbinganService;

    @Qualifier("userDetailsServiceImpl")
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Qualifier("mahasiswaServiceImpl")
    @Autowired
    private MahasiswaServiceImpl mahasiswaService;

    @Qualifier("pembimbingServiceImpl")
    @Autowired
    private PembimbingServiceImpl pembimbingService;

    @Qualifier("ugbServiceImpl")
    @Autowired
    private UgbServiceImpl ugbService;

    @Autowired
    public BaseService baseService;

    @Autowired
    private UserDb userDb;

    @GetMapping("/atur-jadwal/add")
    public String addAvailableBimbinganFormPage(Model model) {
        AvailableBimbinganModel availableBimbingan = new AvailableBimbinganModel();
        model.addAttribute("availableBimbingan", availableBimbingan);
        return "bimbingan/add-available-bimbingan-form";
    }

    @PostMapping("/atur-jadwal/add")
    public String addAvailbaleBimbinganSubmitPage(
        @ModelAttribute AvailableBimbinganModel availableBimbingan, Model model,
        Authentication authentication) {

        String namaUser = authentication.getName();
        UserModel user = userDetailsService.findByUsername(namaUser);

        LocalDateTime startBimbinganTime = availableBimbingan.getStartBimbinganTime();
        LocalDateTime endBimbinganTime = availableBimbingan.getEndBimbinganTime();

        List<AvailableBimbinganModel> overlappingBimbingan = availableBimbinganService.findByStartBimbinganTimeBetween(user.getIdUser(), startBimbinganTime, endBimbinganTime);
        overlappingBimbingan.addAll(availableBimbinganService.findByEndBimbinganTimeBetween(user.getIdUser(), startBimbinganTime, endBimbinganTime));

        AvailableBimbinganModel startBimbinganExist = availableBimbinganService.findByStartBimbinganTime(user.getIdUser(), availableBimbingan.getStartBimbinganTime());
        AvailableBimbinganModel endBimbinganExist = availableBimbinganService.findByEndBimbinganTime(user.getIdUser(), availableBimbingan.getEndBimbinganTime());
        AvailableBimbinganModel startBimbinganBentrok = availableBimbinganService.findByStartBimbinganTime(user.getIdUser(), availableBimbingan.getEndBimbinganTime());
        AvailableBimbinganModel endBimbinganBentrok = availableBimbinganService.findByEndBimbinganTime(user.getIdUser(), availableBimbingan.getStartBimbinganTime());
        
        if (startBimbinganExist == null){
            if (startBimbinganBentrok == null){
                if (endBimbinganExist == null){
                    if (endBimbinganBentrok == null){
                        if (overlappingBimbingan.isEmpty()) {
                            PembimbingModel pembimbing = pembimbingService.findPembimbingById(user.getIdUser()); 
                            availableBimbingan.setPembimbing(pembimbing);
                            availableBimbingan.setBookingStatus("AVAILABLE");
                            availableBimbinganService.add(availableBimbingan);

                            List<AvailableBimbinganModel> listAvailable = availableBimbinganService.findAll();
                            model.addAttribute("listAvailable", listAvailable);
                            return "redirect:/bimbingan/atur-jadwal/";
                        } else {
                            model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                            return "bimbingan/error-bimbingan";
                        }
                    } else {
                        model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                        return "bimbingan/error-bimbingan";
                    } 
                } else {
                    model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                    return "bimbingan/error-bimbingan";
                }
            } else {
                model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                return "bimbingan/error-bimbingan";
        }
        } else {
            model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
            return "bimbingan/error-bimbingan";
        }
    }


    @GetMapping("/atur-jadwal/update/{idAvailableBimbingan}")
    public String updateAvailableBimbinganFormPage(@PathVariable Long idAvailableBimbingan, Model model) {
        AvailableBimbinganModel availableBimbingan = availableBimbinganService.findById(idAvailableBimbingan);
        PembimbingModel pembimbing = availableBimbingan.getPembimbing();
        model.addAttribute("bookingStatus", availableBimbingan.getBookingStatus());
        model.addAttribute("availableBimbingan", availableBimbingan);
        model.addAttribute("pembimbingId", pembimbing.getIdUser());
        return "bimbingan/update-available-bimbingan-form";
    }

    @PostMapping("/atur-jadwal/update")
    public String updateAvailableBimbinganSubmitPage(@ModelAttribute AvailableBimbinganModel availableBimbingan, Authentication authentication, Model model) {
        String namaUser = authentication.getName();
        UserModel user = userDetailsService.findByUsername(namaUser);
        
        LocalDateTime startBimbinganTime = availableBimbingan.getStartBimbinganTime();
        LocalDateTime endBimbinganTime = availableBimbingan.getEndBimbinganTime();

        List<AvailableBimbinganModel> overlappingBimbingan = availableBimbinganService.findByStartBimbinganTimeBetween(user.getIdUser(), startBimbinganTime, endBimbinganTime);
        overlappingBimbingan.addAll(availableBimbinganService.findByEndBimbinganTimeBetween(user.getIdUser(), startBimbinganTime, endBimbinganTime));

        AvailableBimbinganModel startBimbinganExist = availableBimbinganService.findByStartBimbinganTime(user.getIdUser(), availableBimbingan.getStartBimbinganTime());
        AvailableBimbinganModel endBimbinganExist = availableBimbinganService.findByEndBimbinganTime(user.getIdUser(), availableBimbingan.getEndBimbinganTime());
        AvailableBimbinganModel startBimbinganBentrok = availableBimbinganService.findByStartBimbinganTime(user.getIdUser(), availableBimbingan.getEndBimbinganTime());
        AvailableBimbinganModel endBimbinganBentrok = availableBimbinganService.findByEndBimbinganTime(user.getIdUser(), availableBimbingan.getStartBimbinganTime());
        
        if (startBimbinganExist == null){
            if (startBimbinganBentrok == null){
                if (endBimbinganExist == null){
                    if (endBimbinganBentrok == null){
                        if (overlappingBimbingan.isEmpty()) {
                            availableBimbinganService.save(availableBimbingan);
                            List<AvailableBimbinganModel> listAvailable = availableBimbinganService.findAll();
                            model.addAttribute("listAvailable", listAvailable);
                            return "redirect:/bimbingan/atur-jadwal/";
                        } else {
                            model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan sudah bertabrakan ");
                            return "bimbingan/error-bimbingan";
                        }
                    } else {
                        model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                        return "bimbingan/error-bimbingan";
                    } 
                } else {
                    model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                    return "bimbingan/error-bimbingan";
                }
            } else {
                model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
                return "bimbingan/error-bimbingan";
        }
        } else {
            model.addAttribute("pesan", "Waktu mulai bimbingan atau waktu selesai bimbingan bertabrakan ");
            return "bimbingan/error-bimbingan";
        }
    }


    @GetMapping("/viewall")
    public String listBimbingan(Model model, Authentication authentication,
                                @RequestParam(value = "selectedDate", required = false)
                                @DateTimeFormat(iso = ISO.DATE) LocalDate selectedDate) {
        String namaUser = authentication.getName();
        UserModel user = userDetailsService.findByUsername(namaUser);
        if (user.getRoles().contains(EnumRole.PEMBIMBING)) {
            UserModel pembimbing = userDb.findByIdUser(user.getIdUser());
            List<JadwalBimbinganModel> listBimbingan = jadwalBimbinganService.findBimbinganByIdPembimbing(pembimbing.getIdUser());
            if (selectedDate == null) {
                // Jika tidak ada tanggal yang dipilih, set default ke minggu ini
                selectedDate = LocalDate.now();
            }

            LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            // List<JadwalBimbinganModel> listBimbingan = jadwalBimbinganService
            //         .findAllByIdPembimbingAndDateRange(pembimbing.getIdUser(), startOfWeek, endOfWeek);
    
            model.addAttribute("listBimbingan", listBimbingan);
            model.addAttribute("selectedDate", selectedDate);
            return "bimbingan/viewall-jadwal-bimbingan-dosen";

        } else if (user.getRoles().contains(EnumRole.MAHASISWA)){
            MahasiswaModel mahasiswa = mahasiswaService.findMahasiswaById(user.getIdUser());
            List<JadwalBimbinganModel> listBimbingan = jadwalBimbinganService.findBimbinganByIdMahasiswa(mahasiswa.getIdUser());
            model.addAttribute("listBimbingan", listBimbingan);
            return "bimbingan/viewall-jadwal-bimbingan";

        } else{
            model.addAttribute("pesan", "Halaman tidak dapat diakses");
            return "bimbingan/error-bimbingan";
        }
    }
    
    @PostMapping("/isi-catatan/{idJadwalBimbingan}")
    public String isiCatatanBimbingan(@PathVariable Long idJadwalBimbingan, @RequestParam("catatan-bimbingan") String catatanBimbingan,
            Model model) {
        try {
            JadwalBimbinganModel jadwalBimbingan = jadwalBimbinganService.findById(idJadwalBimbingan);
            jadwalBimbingan.setCatatanBimbingan(catatanBimbingan);
            jadwalBimbinganService.update(jadwalBimbingan);

            return "redirect:/bimbingan/viewall/";
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving the file.");
        }
    }

    @PostMapping("/update-catatan/{idJadwalBimbingan}")
    public String updateCatatanBimbingan(@PathVariable Long idJadwalBimbingan, @RequestParam("catatan-bimbingan") String catatanBimbingan,
            Model model) {
        try {
            JadwalBimbinganModel jadwalBimbingan = jadwalBimbinganService.findById(idJadwalBimbingan);
            jadwalBimbingan.setCatatanBimbingan(catatanBimbingan);
            jadwalBimbinganService.update(jadwalBimbingan);

            return "redirect:/bimbingan/viewall/";
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving the file.");
        }
    }


    @GetMapping("/atur-jadwal")
    public String listAvailable(Model model, Authentication authentication,
                                @RequestParam(value = "selectedDate", required = false)
                                @DateTimeFormat(iso = ISO.DATE) LocalDate selectedDate) {
        String namaUser = authentication.getName();
        UserModel user = userDetailsService.findByUsername(namaUser);

        if (user.getRoles().contains(EnumRole.PEMBIMBING)) {
            UserModel pembimbing = userDb.findByIdUser(user.getIdUser());
            // List<AvailableBimbinganModel> listAvailable = availableBimbinganService.findAllByIdPembimbing(pembimbing.getIdUser());
            if (selectedDate == null) {
                // Jika tidak ada tanggal yang dipilih, set default ke minggu ini
                selectedDate = LocalDate.now();
            }

            LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            List<AvailableBimbinganModel> listAvailable = availableBimbinganService
                    .findAllByIdPembimbingAndDateRange(pembimbing.getIdUser(), startOfWeek, endOfWeek);
                    
            model.addAttribute("listAvailable", listAvailable);
            model.addAttribute("selectedDate", selectedDate);
            return "bimbingan/viewall-available-bimbingan";
        } else if (user.getRoles().contains(EnumRole.MAHASISWA)){
            MahasiswaModel mahasiswa = mahasiswaService.findMahasiswaById(user.getIdUser());
            UgbModel ugb = ugbService.findByIdMahasiswa(mahasiswa);
            if (ugb == null){
                model.addAttribute("pesan", "Anda belum melakukan pendaftaran UGB");
                return "bimbingan/error-bimbingan";
            }else {
                if (selectedDate == null) {
                // Jika tidak ada tanggal yang dipilih, set default ke minggu ini
                    selectedDate = LocalDate.now();
                }

                LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                LocalDate endOfWeek = startOfWeek.plusDays(6);

                List<AvailableBimbinganModel> listAvailable = availableBimbinganService.listAvailablePembimbing(ugb, startOfWeek, endOfWeek);
                List<JadwalBimbinganModel> listBimbingan = jadwalBimbinganService.findBimbinganByListAvailable(listAvailable);
                model.addAttribute("user", mahasiswa);
                model.addAttribute("listBimbingan", listBimbingan);
                model.addAttribute("listAvailable", listAvailable);
                model.addAttribute("selectedDate", selectedDate);
                return "bimbingan/viewall-booking-bimbingan";
            }
        } else{
            model.addAttribute("pesan", "Halaman tidak dapat diakses");
            return "bimbingan/error-bimbingan";
        }
    }

    @GetMapping("/atur-jadwal/delete/{idAvailableBimbingan}")
    public String deleteAvailableBimbingan(@PathVariable Long idAvailableBimbingan, Model model) {
        AvailableBimbinganModel availableBimbingan = availableBimbinganService.findById(idAvailableBimbingan);
        availableBimbinganService.delete(idAvailableBimbingan);
        model.addAttribute("availableBimbingan", availableBimbingan);
        return "redirect:/bimbingan/atur-jadwal/";
    }

    @PostMapping("/atur-jadwal/booking/{idAvailableBimbingan}")
    public String bookingJadwalBimbingan(@PathVariable Long idAvailableBimbingan, Model model, Authentication authentication) {
        String namaUser = authentication.getName();
        UserModel user = userDetailsService.findByUsername(namaUser);
        MahasiswaModel mahasiswa = mahasiswaService.findMahasiswaById(user.getIdUser());

        JadwalBimbinganModel jadwalBimbingan = new JadwalBimbinganModel();
        AvailableBimbinganModel availableBimbingan = availableBimbinganService.findById(idAvailableBimbingan);
        availableBimbingan.setMahasiswa(mahasiswa);
        jadwalBimbingan.setAvailableBimbingan(availableBimbingan);
        jadwalBimbingan.setMahasiswa(mahasiswa);
        jadwalBimbinganService.save(jadwalBimbingan);

        availableBimbingan.setBookingStatus("BOOKED");
        availableBimbinganService.save(availableBimbingan);
        model.addAttribute("availableBimbingan", availableBimbingan);
        return "redirect:/bimbingan/atur-jadwal/";
    }

    @GetMapping("/atur-jadwal/cancel/{idAvailableBimbingan}")
    public String cancelJadwalBimbingan(@PathVariable Long idAvailableBimbingan, Model model) {
        JadwalBimbinganModel jadwalBimbingan = jadwalBimbinganService.findByAvaialableBimbingan(idAvailableBimbingan);
        jadwalBimbinganService.delete(jadwalBimbingan.getIdJadwalBimbingan());

        AvailableBimbinganModel availableBimbingan = availableBimbinganService.findById(idAvailableBimbingan);
        availableBimbingan.setMahasiswa(null);
        availableBimbingan.setBookingStatus("AVAILABLE");
        availableBimbinganService.save(availableBimbingan);
        model.addAttribute("availableBimbingan", availableBimbingan);
        return "redirect:/bimbingan/atur-jadwal/";
    }
    
}
