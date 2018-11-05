package br.net.fabiozumbi12.RedProtect.Sponge;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RPVHelper56 implements RPVHelper {
	
	RPVHelper56(){}
	
	@Override
	public Cause getCause(CommandSource p) {
		return Cause.of(NamedCause.simulated(p));
	}

    @Override
	public void closeInventory(Player p) {
		p.closeInventory(getCause(p));
	}

	@Override
	public void openInventory(Inventory inv, Player p) {
		p.openInventory(inv, Cause.of(NamedCause.of(p.getName(),p)));
	}

	@Override
	public void setBlock(Location<World> loc, BlockState block) {
		loc.setBlockType(block.getType(), Cause.of(NamedCause.owner(RedProtect.get().container)));
	}

	@Override
	public void digBlock(Player p, ItemStack item, Vector3i loc) {
		p.getWorld().digBlockWith(loc, item, Cause.of(NamedCause.owner(RedProtect.get().container)));
	}

	@Override
	public void digBlock(Player p, Vector3i loc) {
		p.getWorld().digBlock(loc, Cause.of(NamedCause.owner(RedProtect.get().container)));
	}

	@Override
	public void removeBlock(Location<World> loc) {
		loc.removeBlock(Cause.of(NamedCause.owner(RedProtect.get().container)));
	}

	@Override
	public boolean checkCause(Cause cause, String toCompare) {
		return cause.containsNamed(toCompare);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean checkHorseOwner(Entity ent, Player p) {
		if (ent instanceof Horse && ((Horse)ent).getHorseData().get(Keys.TAMED_OWNER).isPresent()){
			Horse tam = (Horse) ent;
			Player owner = RedProtect.get().serv.getPlayer(tam.getHorseData().get(Keys.TAMED_OWNER).get().get()).get();
			return owner.getName().equals(p.getName());
		}
		return false;
	}

	@Override
	public List<String> getAllEnchants() {
		return Sponge.getRegistry().getAllOf(Enchantment.class).stream().map(Enchantment::getId).collect(Collectors.toList());
	}

	@Override
	public ItemStack offerEnchantment(ItemStack item) {
		item.offer(Keys.ITEM_ENCHANTMENTS, Collections.singletonList(new ItemEnchantment(Enchantments.UNBREAKING, 1)));
		return item;
	}

	@Override
	public long getInvValue(Iterable<Inventory> inv){
		long value = 0;
		for (Inventory item:inv){
			if (!item.peek().isPresent()){
				continue;
			}
			ItemStack stack = item.peek().get();
			value += ((RedProtect.get().cfgs.getBlockCost(stack.getItem().getId()) * stack.getQuantity()));
			if (stack.get(Keys.ITEM_ENCHANTMENTS).isPresent()){
				for (ItemEnchantment enchant:stack.get(Keys.ITEM_ENCHANTMENTS).get()){
					value += ((RedProtect.get().cfgs.getEnchantCost(enchant.getEnchantment().getId()) * enchant.getLevel()));
				}
			}
		}
		return value;
	}

	@Override
	public Inventory query(Inventory inventory, int x, int y){
		return inventory.query(SlotPos.of(x, y));
	}

	@Override
	public ItemStack getItemMainHand(Player player){
		if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent())
			return player.getItemInHand(HandTypes.MAIN_HAND).get();

		return ItemStack.empty();
	}

	@Override
	public ItemStack getItemOffHand(Player player){
		if (player.getItemInHand(HandTypes.OFF_HAND).isPresent())
			return player.getItemInHand(HandTypes.OFF_HAND).get();

		return ItemStack.empty();
	}

	@Override
	public ItemType getItemInHand(Player player){
		if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
			return player.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
		} else if (player.getItemInHand(HandTypes.OFF_HAND).isPresent()){
			return player.getItemInHand(HandTypes.OFF_HAND).get().getItem();
		}
		return ItemTypes.NONE;
	}

	@Override
	public ItemType getItemType(ItemStack itemStack){
		return itemStack.getItem();
	}

	@Override
	public Inventory newInventory(int size, String name){
		return Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
				.property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, size/9))
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(RPUtil.toText(name)))
				.build(RedProtect.get().container);
	}

	@Override
	public void removeGuiItem(Player p){
		p.getInventory().slots().forEach(slot -> {
			if (slot.peek().isPresent()){
				ItemStack pitem = slot.peek().get();
				if (RPUtil.removeGuiItem(pitem)){
					slot.poll().get();
				}
			}
		});
	}
}
