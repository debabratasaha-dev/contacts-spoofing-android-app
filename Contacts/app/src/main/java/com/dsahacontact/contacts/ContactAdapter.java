package com.dsahacontact.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList;
    private List<Contact> fullList;

    public ContactAdapter(List<Contact> contactList) {
        // create mutable copies
        this.contactList = new ArrayList<>(contactList);
        this.fullList = new ArrayList<>(contactList);
    }

    public void updateData(List<Contact> newContacts) {
        // update both lists when data arrives
        fullList = new ArrayList<>(newContacts);
        contactList.clear();
        contactList.addAll(newContacts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.name.setText(contact.getName());
        holder.number.setText(contact.getNumber());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView name, number;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(android.R.id.text1);
            number = itemView.findViewById(android.R.id.text2);
        }
    }

    public void filter(String text) {
        contactList.clear();
        if (text.isEmpty()) {
            contactList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (Contact contact : fullList) {
                if (contact.getName().toLowerCase().contains(text) ||
                        contact.getNumber().contains(text)) {
                    contactList.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }
}
