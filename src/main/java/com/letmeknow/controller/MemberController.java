//package com.letmeknow.controller;
//
//import com.letmeknow.exception.member.NoSuchMemberException;
//import com.letmeknow.form.MemberAddressUpdateForm;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import com.letmeknow.dto.member.MemberFindDto;
//import com.letmeknow.dto.member.MemberUpdateDto;
//import com.letmeknow.dto.store.StoreDto;
//import com.letmeknow.service.member.MemberService;
//import com.letmeknow.service.store.StoreService;
//
//import javax.validation.Valid;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class MemberController {
//
//    private final MemberService memberService;
//    private final StoreService storeService;
//
//    @GetMapping("/members")
//    @PreAuthorize("hasRole('ROLE_MEMBER')")
//    public String memberList(Model model) {
//        List<MemberFindDto> findAllMemberFindDto = memberService.findAllMemberFindDto();
//
//        model.addAttribute("memberFindDtos", findAllMemberFindDto);
//
//        return "member/memberList";
//    }
//
//    @GetMapping("/members/{memberId}")
//    @PreAuthorize("hasRole('ROLE_MEMBER')")
//    public String memberInfo(@PathVariable("memberId") long memberId, Model model) throws NoSuchMemberException {
//        MemberFindDto memberFindDto = memberService.findMemberFindDtoById(memberId);
//        List<StoreDto> storeDtos = storeService.findAllStoreDtoById(memberId);
//        model.addAttribute("memberFindDto", memberFindDto);
//        model.addAttribute("storeDtos", storeDtos);
//        return "member/memberInfo";
//    }
//
////    @GetMapping("/members/new")
////    public String memberCreationForm(Model model) {
////        model.addAttribute("memberCreationForm", new MemberSignUpForm());
////
////        return "member/memberCreationForm";
////    }
////
////    @PostMapping("/members/new")
////    public String createMember(@Valid MemberSignUpForm memberSignUpForm, BindingResult result) {
////
////        if (result.hasErrors()) {
////            return "member/memberCreationForm";
////        }
////
////        long savedMemberId = memberService.joinMember(MemberCreationDto.builder()
////                .name(memberSignUpForm.getName())
////                .email(memberSignUpForm.getEmail())
////                .password(memberSignUpForm.getPassword())
////                .city(memberSignUpForm.getCity())
////                .street(memberSignUpForm.getStreet())
////                .zipcode(memberSignUpForm.getZipcode())
////                .build());
////
////        return "redirect:/members/"+savedMemberId;
////    }
//
//    @GetMapping("/members/{memberId}/update")
//    @PreAuthorize("hasRole('ROLE_MEMBER')")
//    public String memberUpdateForm(@PathVariable("memberId") long memberId, Model model) throws NoSuchMemberException {
//        model.addAttribute("memberFindDto", memberService.findMemberFindDtoById(memberId));
//        model.addAttribute("memberUpdateForm", new MemberAddressUpdateForm());
//
//        return "member/memberUpdateForm";
//    }
//
//    @PostMapping("/members/update")
//    public String updateMember(@Valid MemberAddressUpdateForm memberUpdateForm, BindingResult result, Model model) throws NoSuchMemberException {
//        if (result.hasErrors()) {
//            model.addAttribute("memberFindDto", memberService.findMemberFindDtoById(memberId));
//            return "member/memberUpdateForm";
//        }
//
//        long savedMemberId = memberService.updateMemberAddress(MemberUpdateDto.builder()
//            .id(memberId)   //나중에 memberId 검증할 것
//            .name(memberUpdateForm.getName())
//            .city(memberUpdateForm.getCity())
//            .street(memberUpdateForm.getStreet())
//            .zipcode(memberUpdateForm.getZipcode())
//            .build());
//
//        return "redirect:/members/" + savedMemberId;
//    }
//}
