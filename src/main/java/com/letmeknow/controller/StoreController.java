package com.letmeknow.controller;

import com.letmeknow.exception.member.NoSuchMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.letmeknow.dto.store.StoreCreationDto;
import com.letmeknow.dto.store.StoreToggleStatusDto;
import com.letmeknow.dto.store.StoreUpdateDto;
import com.letmeknow.form.StoreCreationForm;
import com.letmeknow.form.StoreUpdateForm;
import com.letmeknow.service.member.MemberService;
import com.letmeknow.service.store.StoreService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final MemberService memberService;
    private final StoreService storeService;

    @GetMapping("/members/{memberId}/stores/{storeId}")
    public String storeInfo(@PathVariable("memberId") long memberId, @PathVariable("storeId") long storeId, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("storeId", storeId);

        return "store/storeInfo";
    }

    @GetMapping("/members/{memberId}/stores/new")
    public String createStoreForm(@PathVariable("memberId") long memberId, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("storeCreationForm", new StoreCreationForm());

        return "store/storeCreationForm";
    }

    @PostMapping("/members/{memberId}/stores/new")
    public String createStore(@PathVariable("memberId") long memberId, @Valid StoreCreationForm storeCreationForm, BindingResult result) throws NoSuchMemberException {

        if (result.hasErrors()) {
            return "store/storeCreationForm";
        }

        long createdStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(memberId)
                .name(storeCreationForm.getName())
                .city(storeCreationForm.getCity())
                .street(storeCreationForm.getStreet())
                .zipcode(storeCreationForm.getZipcode())
                .build());

        return "redirect:/members/"+memberId;
    }

    @GetMapping("/members/{memberId}/stores/{storeId}/update")
    public String updateStoreForm(@PathVariable("storeId") long storeId, Model model) {
        model.addAttribute("storeUpdateForm", new StoreUpdateForm());

        return "store/storeUpdateForm";
    }

    @PostMapping("/members/{memberId}/stores/{storeId}/update")
    public String updateStore(@PathVariable("memberId") long memberId, @PathVariable("storeId") long storeId, @Valid StoreUpdateForm storeUpdateForm, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("storeUpdateForm", new StoreUpdateForm());
            return "store/storeUpdateForm";
        }

        long updatedStoreId = storeService.updateStore(StoreUpdateDto.builder()
                .memberId(memberId) //나중에 memberId 검증할 것
                .id(storeId) //나중에 storeId 검증할 것
                .name(storeUpdateForm.getName())
                .city(storeUpdateForm.getCity())
                .street(storeUpdateForm.getStreet())
                .zipcode(storeUpdateForm.getZipcode())
                .build());

        return "redirect:/members/"+memberId+"/stores/"+updatedStoreId;
    }

    @GetMapping("/members/{memberId}/stores/{storeId}/changeStoreStatus")
    public String changeStoreStatus(@PathVariable("memberId") long memberId, @PathVariable("storeId") long storeId) {
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeId)
                .memberId(memberId)
                .build());

        return "redirect:/members/"+memberId+"/stores/"+storeId;
    }
}
