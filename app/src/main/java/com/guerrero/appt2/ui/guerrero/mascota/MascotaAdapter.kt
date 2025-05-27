package com.guerrero.appt2.ui.guerrero.mascota

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guerrero.appt2.R
import com.guerrero.appt2.data.guerrero.model.Mascota
import com.guerrero.appt2.databinding.ItemMascotaBinding


class MascotaAdapter(
    private val onEditClick: (Mascota) -> Unit,
    private val onDeleteClick: (Mascota) -> Unit
) : ListAdapter<Mascota, MascotaAdapter.MascotaViewHolder>(PetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val binding = ItemMascotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MascotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = getItem(position)
        holder.bind(mascota)
    }

    inner class MascotaViewHolder(private val binding: ItemMascotaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnEditPet.setOnClickListener {
                onEditClick(getItem(adapterPosition))
            }
            binding.btnDeletePet.setOnClickListener {
                showDeleteConfirmationDialog()
            }
        }

        fun bind(pet: Mascota) {
            binding.tvPetName.text = pet.name
            binding.tvPetType.text = binding.root.context.getString(R.string.pet_type_label, pet.type)
            binding.tvPetAge.text = binding.root.context.getString(R.string.pet_age_label, pet.age.toString())
            binding.ivPetImage.contentDescription = binding.root.context.getString(R.string.pet_image_content_description, pet.name)
            Glide.with(binding.root.context)
                .load(pet.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivPetImage)
        }

        private fun showDeleteConfirmationDialog() {
            val context = binding.root.context
            val pet = getItem(adapterPosition)
            AlertDialog.Builder(context)
                .setTitle(R.string.pet_delete_confirmation_title)
                .setMessage(context.getString(R.string.pet_delete_confirmation_message, pet.name))
                .setPositiveButton(R.string.pet_delete_confirm_button) { _, _ ->
                    onDeleteClick(pet)
                }
                .setNegativeButton(R.string.pet_delete_cancel_button, null)
                .show()
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Mascota>() {
        override fun areItemsTheSame(oldItem: Mascota, newItem: Mascota): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mascota, newItem: Mascota): Boolean {
            return oldItem == newItem
        }
    }
}